USE railway_db;

-- Add new trains using existing stations

-- 1. Gatimaan Express (New Delhi -> Jaipur)
INSERT INTO trains (arrival_time, available_seats, base_fare, departure_time, latitude, longitude, name, total_seats, train_number, train_type, from_station_id, to_station_id)
VALUES (DATE_ADD(NOW(), INTERVAL '1 10:30' DAY_MINUTE), 250, 800.00, DATE_ADD(NOW(), INTERVAL '1 06:15' DAY_MINUTE), 26.9124, 75.7873, 'Gatimaan Express', 400, '12049', 'EXPRESS', 1, 7);

-- 2. Vande Bharat Express (New Delhi -> Lucknow)
INSERT INTO trains (arrival_time, available_seats, base_fare, departure_time, latitude, longitude, name, total_seats, train_number, train_type, from_station_id, to_station_id)
VALUES (DATE_ADD(NOW(), INTERVAL '0 12:45' DAY_MINUTE), 450, 1100.00, DATE_ADD(NOW(), INTERVAL '0 06:00' DAY_MINUTE), 26.8467, 80.9462, 'Vande Bharat Express', 500, '22435', 'EXPRESS', 1, 10);

-- 3. Pune-Mumbai Intercity (Pune -> Mumbai)
INSERT INTO trains (arrival_time, available_seats, base_fare, departure_time, latitude, longitude, name, total_seats, train_number, train_type, from_station_id, to_station_id)
VALUES (DATE_ADD(NOW(), INTERVAL '0 11:20' DAY_MINUTE), 100, 250.00, DATE_ADD(NOW(), INTERVAL '0 08:15' DAY_MINUTE), 19.0760, 72.8777, 'Pune-Mumbai Intercity', 250, '12128', 'EXPRESS', 9, 2);

-- 4. Charminar Express (Chennai -> Hyderabad)
INSERT INTO trains (arrival_time, available_seats, base_fare, departure_time, latitude, longitude, name, total_seats, train_number, train_type, from_station_id, to_station_id)
VALUES (DATE_ADD(NOW(), INTERVAL '1 09:30' DAY_MINUTE), 220, 600.00, DATE_ADD(NOW(), INTERVAL '0 18:00' DAY_MINUTE), 17.3850, 78.4867, 'Charminar Express', 600, '12759', 'EXPRESS', 3, 6);

-- 5. Howrah SF Express (Kolkata -> Bengaluru)
INSERT INTO trains (arrival_time, available_seats, base_fare, departure_time, latitude, longitude, name, total_seats, train_number, train_type, from_station_id, to_station_id)
VALUES (DATE_ADD(NOW(), INTERVAL '2 15:45' DAY_MINUTE), 110, 1400.00, DATE_ADD(NOW(), INTERVAL '1 09:15' DAY_MINUTE), 12.9716, 77.5946, 'Howrah SF Express', 550, '12323', 'EXPRESS', 4, 5);


-- Now add their positions
INSERT INTO train_positions (current_station, latitude, longitude, speed, train_id, updated_at)
SELECT 'New Delhi', 28.6139, 77.2090, 0, id, NOW() FROM trains WHERE train_number = '12049';

INSERT INTO train_positions (current_station, latitude, longitude, speed, train_id, updated_at)
SELECT 'New Delhi', 28.6139, 77.2090, 0, id, NOW() FROM trains WHERE train_number = '22435';

INSERT INTO train_positions (current_station, latitude, longitude, speed, train_id, updated_at)
SELECT 'Pune Junction', 18.5204, 73.8567, 0, id, NOW() FROM trains WHERE train_number = '12128';

INSERT INTO train_positions (current_station, latitude, longitude, speed, train_id, updated_at)
SELECT 'Chennai Central', 13.0827, 80.2707, 0, id, NOW() FROM trains WHERE train_number = '12759';

INSERT INTO train_positions (current_station, latitude, longitude, speed, train_id, updated_at)
SELECT 'Kolkata Howrah', 22.5726, 88.3639, 0, id, NOW() FROM trains WHERE train_number = '12323';
