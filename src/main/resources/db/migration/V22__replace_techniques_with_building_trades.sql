-- V22__replace_techniques_with_building_trades.sql
-- Replace legacy handicraft techniques with authentic Algerian building-trade techniques.

-- 1. Preserve IDs of artisans who currently hold techniques
CREATE TEMPORARY TABLE IF NOT EXISTS temp_artisan_technique_holders AS
    SELECT DISTINCT artisan_id FROM artisan_techniques;

-- 2. Remove join table links to old techniques
DELETE FROM artisan_techniques;

-- 3. Delete legacy handicraft technique taxonomy rows
DELETE FROM techniques;

-- 4. Seed the 7 building-trade techniques with deterministic UUIDs
INSERT INTO techniques (id, name, slug, description, display_order, is_active, created_at, updated_at) VALUES
('b0010001-0000-0000-0000-000000000001', 'Pisé', 'pise', 'Technique de construction traditionnelle en terre crue compactée dans des coffrages, offrant une forte inertie thermique.', 1, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
('b0010001-0000-0000-0000-000000000002', 'Enduit à la chaux', 'enduit-a-la-chaux', 'Application d''enduits respirants à base de chaux aérienne ou hydraulique pour la protection et la finition des maçonneries anciennes.', 2, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
('b0010001-0000-0000-0000-000000000003', 'Taille de pierre', 'taille-de-pierre', 'Façonnage et appareillage de blocs de pierre naturelle pour la construction d''ouvrages porteurs ou la restauration de modénatures.', 3, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
('b0010001-0000-0000-0000-000000000004', 'Maçonnerie en moellons', 'maconnerie-en-moellons', 'Montage de murs en pierres brutes ou équarries liées au mortier traditionnel, assurant solidité et intégration paysagère.', 4, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
('b0010001-0000-0000-0000-000000000005', 'Zellij', 'zellij', 'Art décoratif de mosaïque géométrique en terre cuite émaillée, découpée et assemblée manuellement selon des motifs traditionnels.', 5, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
('b0010001-0000-0000-0000-000000000006', 'Géjij (plâtre sculpté)', 'gejij-platre-sculpte', 'Sculpture ornementale et ciselure manuelle sur plâtre traditionnel frais pour les frises, arcs, coupoles et plafonds décorés.', 6, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
('b0010001-0000-0000-0000-000000000007', 'Charpente traditionnelle', 'charpente-traditionnelle', 'Conception, assemblage à tenons et mortaises et pose de structures porteuses en bois massif pour toitures et planchers.', 7, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));

-- 5. Restore technique links for artisans who had techniques
INSERT IGNORE INTO artisan_techniques (artisan_id, technique_id)
SELECT t.artisan_id, tech.id
FROM temp_artisan_technique_holders t
JOIN techniques tech ON tech.id IN (
    'b0010001-0000-0000-0000-000000000001',
    'b0010001-0000-0000-0000-000000000002'
);

DROP TEMPORARY TABLE IF EXISTS temp_artisan_technique_holders;
