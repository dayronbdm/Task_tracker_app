INSERT INTO category (name) VALUES ('Work');
INSERT INTO category (name) VALUES ('Personal');
INSERT INTO category (name) VALUES ('Shopping');
INSERT INTO category (name) VALUES ('Health');

INSERT INTO task (title, description, completed, category_id) VALUES
  ('Finish project report',   'Complete the Q2 report and send to manager',        false, 1),
  ('Schedule team meeting',   'Set up weekly sync for the engineering team',        true,  1),
  ('Review pull requests',    'Go through open PRs and leave feedback',             false, 1),
  ('Read a book',             'Finish reading "Clean Code" by Robert C. Martin',    false, 2),
  ('Call mom',                'Weekly catch-up call on Sunday afternoon',           false, 2),
  ('Plan weekend trip',       'Research destinations and book accommodation',       false, 2),
  ('Buy groceries',           'Milk, eggs, bread, vegetables, and fruit',           false, 3),
  ('Order new headphones',    'Look for noise-cancelling headphones under $150',    true,  3),
  ('Morning run',             '5 km run in the park before breakfast',              true,  4),
  ('Doctor appointment',      'Annual check-up scheduled for next Monday',         false, 4);
