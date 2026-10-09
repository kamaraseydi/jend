
-- Annonces publiques : statut et date de publication
CREATE INDEX idx_annonce_statut_created_at
    ON annonce (statut, created_at DESC);

-- Annonces d'un vendeur, triées par date
CREATE INDEX idx_annonce_vendeur_created_at
    ON annonce (vendeur_id, created_at DESC);

-- Annonces filtrées par catégorie et statut
CREATE INDEX idx_annonce_category_statut_created_at
    ON annonce (category_id, statut, created_at DESC);

-- Recherche des favoris associés à une annonce
CREATE INDEX idx_favorite_annonce_id
    ON favorite (annonce_id);
