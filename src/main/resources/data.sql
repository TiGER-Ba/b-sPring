-- ===== src/main/resources/data.sql =====
-- Données initiales pour l'application Bankati Spring Boot

-- Insertion d'un utilisateur administrateur
-- Mot de passe: admin (encodé avec BCrypt)
INSERT INTO users (first_name, last_name, username, password, role, creation_date, enabled, account_non_expired, account_non_locked, credentials_non_expired)
VALUES ('Admin', 'Bankati', 'admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'ADMIN', CURDATE(), true, true, true, true)
    ON DUPLICATE KEY UPDATE id=id;

-- Insertion d'un utilisateur client de test
-- Mot de passe: user (encodé avec BCrypt)
INSERT INTO users (first_name, last_name, username, password, role, creation_date, enabled, account_non_expired, account_non_locked, credentials_non_expired)
VALUES ('John', 'Doe', 'user', '$2a$10$DowJones2021.Hash.Example.Encoded.Password.Here', 'USER', CURDATE(), true, true, true, true)
    ON DUPLICATE KEY UPDATE id=id;

-- Insertion d'autres clients de test
INSERT INTO users (first_name, last_name, username, password, role, creation_date, enabled, account_non_expired, account_non_locked, credentials_non_expired)
VALUES
    ('Marie', 'Martin', 'marie', '$2a$10$DowJones2021.Hash.Example.Encoded.Password.Here', 'USER', CURDATE(), true, true, true, true),
    ('Ahmed', 'Benali', 'ahmed', '$2a$10$DowJones2021.Hash.Example.Encoded.Password.Here', 'USER', CURDATE(), true, true, true, true),
    ('Sarah', 'Alami', 'sarah', '$2a$10$DowJones2021.Hash.Example.Encoded.Password.Here', 'USER', CURDATE(), true, true, true, true)
    ON DUPLICATE KEY UPDATE id=id;

-- Insertion de quelques demandes de crédit de test
INSERT INTO credit_requests (client_id, amount, duration, description, request_date, status, created_at)
VALUES
    ((SELECT id FROM users WHERE username = 'user'), 15000.00, 24, 'Achat d''une voiture familiale', CURDATE() - INTERVAL 5 DAY, 'PENDING', CURDATE() - INTERVAL 5 DAY),
    ((SELECT id FROM users WHERE username = 'marie'), 50000.00, 60, 'Rénovation de ma maison', CURDATE() - INTERVAL 10 DAY, 'APPROVED', CURDATE() - INTERVAL 10 DAY),
    ((SELECT id FROM users WHERE username = 'ahmed'), 8000.00, 12, 'Financement d''études', CURDATE() - INTERVAL 3 DAY, 'PENDING', CURDATE() - INTERVAL 3 DAY),
    ((SELECT id FROM users WHERE username = 'sarah'), 25000.00, 36, 'Création d''entreprise', CURDATE() - INTERVAL 7 DAY, 'REJECTED', CURDATE() - INTERVAL 7 DAY)
    ON DUPLICATE KEY UPDATE id=id;

-- Mise à jour des demandes approuvées et rejetées avec les dates de traitement
UPDATE credit_requests
SET processed_date = CURDATE() - INTERVAL 2 DAY,
    admin_comment = 'Dossier complet et solvabilité confirmée'
WHERE status = 'APPROVED';

UPDATE credit_requests
SET processed_date = CURDATE() - INTERVAL 1 DAY,
    admin_comment = 'Revenus insuffisants par rapport au montant demandé'
WHERE status = 'REJECTED';