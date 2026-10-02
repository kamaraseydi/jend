CREATE TABLE user_profile (
      id BIGSERIAL PRIMARY KEY,

      supabase_user_id VARCHAR(255) NOT NULL UNIQUE,

      nom VARCHAR(100) NOT NULL,
      email VARCHAR(255) NOT NULL UNIQUE,
      telephone VARCHAR(30),
      ville VARCHAR(100),

      est_professionnel BOOLEAN NOT NULL DEFAULT FALSE,
      suspendu BOOLEAN NOT NULL DEFAULT FALSE,

      deleted_at TIMESTAMPTZ,

      role VARCHAR(30) NOT NULL,

      created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

      CONSTRAINT chk_user_role
          CHECK (role IN ('ADMIN', 'UTILISATEUR'))
);


CREATE TABLE category (
      id BIGSERIAL PRIMARY KEY,

      nom VARCHAR(100) NOT NULL,

      parent_id BIGINT,

      created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

      CONSTRAINT fk_category_parent
          FOREIGN KEY (parent_id)
              REFERENCES category(id)
);


CREATE TABLE annonce (
     id BIGSERIAL PRIMARY KEY,

     titre VARCHAR(150) NOT NULL,
     description VARCHAR(5000) NOT NULL,

     prix NUMERIC(12, 2) NOT NULL,

     etat VARCHAR(30) NOT NULL,
     statut VARCHAR(30) NOT NULL,

     ville VARCHAR(100) NOT NULL,
     quartier VARCHAR(100),

     vendeur_id BIGINT NOT NULL,
     category_id BIGINT NOT NULL,

     created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

     CONSTRAINT fk_annonce_vendeur
         FOREIGN KEY (vendeur_id)
             REFERENCES user_profile(id),

     CONSTRAINT fk_annonce_category
         FOREIGN KEY (category_id)
             REFERENCES category(id),

     CONSTRAINT chk_annonce_prix
         CHECK (prix >= 0),

     CONSTRAINT chk_annonce_etat
         CHECK (
             etat IN (
                      'NEUF',
                      'COMME_NEUF',
                      'TRES_BON_ETAT',
                      'BON_ETAT',
                      'A_REPARER'
                 )
             ),

     CONSTRAINT chk_annonce_statut
         CHECK (
             statut IN (
                        'BROUILLON',
                        'PUBLIEE',
                        'SUSPENDUE',
                        'VENDUE',
                        'SUPPRIMEE'
                 )
             )
);


CREATE TABLE annonce_image (
       id BIGSERIAL PRIMARY KEY,

       annonce_id BIGINT NOT NULL,

       url VARCHAR(1000) NOT NULL,
       ordre INTEGER NOT NULL,

       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

       CONSTRAINT fk_annonce_image_annonce
           FOREIGN KEY (annonce_id)
               REFERENCES annonce(id)
               ON DELETE CASCADE,

       CONSTRAINT chk_annonce_image_ordre
           CHECK (ordre >= 1 AND ordre <= 8),

       CONSTRAINT uk_annonce_image_ordre
           UNIQUE (annonce_id, ordre)
);


CREATE TABLE favorite (
      id BIGSERIAL PRIMARY KEY,

      user_id BIGINT NOT NULL,
      annonce_id BIGINT NOT NULL,

      created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

      CONSTRAINT fk_favorite_user
          FOREIGN KEY (user_id)
              REFERENCES user_profile(id),

      CONSTRAINT fk_favorite_annonce
          FOREIGN KEY (annonce_id)
              REFERENCES annonce(id)
              ON DELETE CASCADE,

      CONSTRAINT uk_favorite_user_annonce
          UNIQUE (user_id, annonce_id)
);