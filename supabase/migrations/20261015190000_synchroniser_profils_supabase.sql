
-- Synchronise les utilisateurs Supabase Auth avec les profils Jend.

CREATE OR REPLACE FUNCTION public.handle_new_supabase_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
DECLARE
    nom_utilisateur TEXT;
BEGIN
    IF NEW.email IS NULL OR btrim(NEW.email) = '' THEN
        RAISE EXCEPTION 'Jend requires an email address';
    END IF;

    nom_utilisateur := LEFT(
        COALESCE(
            NULLIF(btrim(NEW.raw_user_meta_data ->> 'nom'), ''),
            NULLIF(btrim(NEW.raw_user_meta_data ->> 'full_name'), ''),
            split_part(NEW.email, '@', 1)
        ),
        100
    );

    IF EXISTS (
        SELECT 1
        FROM public.user_profile p
        WHERE lower(p.email) = lower(NEW.email)
          AND p.supabase_user_id <> NEW.id::TEXT
    ) THEN
        RAISE EXCEPTION 'Email already linked to another Jend profile';
    END IF;

    INSERT INTO public.user_profile (
        supabase_user_id,
        nom,
        email,
        role
    )
    VALUES (
        NEW.id::TEXT,
        nom_utilisateur,
        NEW.email,
        'UTILISATEUR'
    )
    ON CONFLICT (supabase_user_id) DO NOTHING;

    RETURN NEW;
END;
$$;

REVOKE ALL ON FUNCTION public.handle_new_supabase_user()
FROM PUBLIC, anon, authenticated;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_trigger t
        JOIN pg_class c ON c.oid = t.tgrelid
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE n.nspname = 'auth'
          AND c.relname = 'users'
          AND t.tgname = 'on_auth_user_created'
          AND NOT t.tgisinternal
    ) THEN
        CREATE TRIGGER on_auth_user_created
        AFTER INSERT ON auth.users
        FOR EACH ROW
        EXECUTE FUNCTION public.handle_new_supabase_user();
    END IF;
END;
$$;

-- Arrête la migration si un compte Auth existant entre en conflit
-- avec un profil Jend appartenant à un autre identifiant Supabase.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM auth.users u
        JOIN public.user_profile p
          ON lower(p.email) = lower(u.email)
         AND p.supabase_user_id <> u.id::TEXT
        WHERE u.email IS NOT NULL
    ) THEN
        RAISE EXCEPTION 'Email conflict detected between Auth users and Jend profiles';
    END IF;
END;
$$;

-- Synchronise les comptes Auth existants.
INSERT INTO public.user_profile (
    supabase_user_id,
    nom,
    email,
    role
)
SELECT
    u.id::TEXT,
    LEFT(
        COALESCE(
            NULLIF(btrim(u.raw_user_meta_data ->> 'nom'), ''),
            NULLIF(btrim(u.raw_user_meta_data ->> 'full_name'), ''),
            split_part(u.email, '@', 1)
        ),
        100
    ),
    u.email,
    'UTILISATEUR'
FROM auth.users u
WHERE u.email IS NOT NULL
  AND btrim(u.email) <> ''
ON CONFLICT (supabase_user_id) DO NOTHING;
