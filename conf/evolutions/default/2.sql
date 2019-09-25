-- !Ups

-- Add countries
INSERT INTO CountryDefinition (id, name) VALUES
(4, 'Afghanistan'),
(32, 'Argentina'),
(36, 'Australia'),
(76, 'Brazil'),
(124, 'Canada'),
(156, 'China'),
(203, 'Czech Republic'),
(208, 'Denmark'),
(818, 'Egypt'),
(246, 'Finland'),
(250, 'France'),
(276, 'Germany'),
(300, 'Greece'),
(344, 'Hong Kong'),
(348, 'Hungary'),
(356, 'India'),
(380, 'Italy'),
(392, 'Japan'),
(398, 'Kazakhstan'),
(404, 'Kenya'),
(408, 'Korea (Democratic People''s Republic of)'),
(434, 'Libya'),
(458, 'Malaysia'),
(484, 'Mexico'),
(496, 'Mongolia'),
(524, 'Nepal'),
(554, 'New Zealand'),
(608, 'Philippines'),
(630, 'Puerto Rico'),
(643, 'Russian Federation'),
(682, 'Saudi Arabia'),
(702, 'Singapore'),
(710, 'South Africa'),
(410, 'South Korea'),
(724, 'Spain'),
(158, 'Taiwan'),
(764, 'Thailand'),
(792, 'Turkey'),
(784, 'United Arab Emirates'),
(826, 'United Kingdom'),
(840, 'United States of America');

-- Add sample user
INSERT INTO User (id, username, password, salt, admin, deleted, creation_date) VALUES
(1, 'admin@travelea.co.nz', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=', 1, 0, '2019-09-25 12:31:41'),
(2, 'testUser@email.com', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=', 0, 0, '2019-09-25 12:31:41'),
(3, 'testUser2@email.com', '51i2xJJXKnRNYfO3+UXOveorYfd8bTIDlqUcE8c50lM=', 'tujlegP8Dc8dQ19Ad6ekgVla3d7qbtb9iHiTJ2VRssQ=', 0, 0, '2019-09-25 12:31:41'),
(4, 'williamwallace@email.com', 'rH8wCZdNsEB3CqlYLJ33sJop0tdmvdXcsVALfS3y7ko=', 'bXoxpeMvRnPXODHBsrLYk6SDIvfVL+8FrLQ+2hyZ0cU=', 0, 0, '2019-09-25 12:31:55'),
(5, 'maxandrew@gmail.com', 'otFBJmWBs2fsVbVsDxikZwUyj3/vTbU60WIhy9M3Wg8=', 'NW6gOZqG622LTlxMyCar45azEn54NzUGtR5EK6vz9Pg=', 0, 0, '2019-09-25 12:48:30'),
(6, 'ryanchen@hotmail.com', 'qL/LLscp61nEHUCvFsRUdh7y/Ralnd27FE6OFGRdj80=', 'oH/cJsdv/0pSV74I3mL8gglU0gLypZTnYisYxcMzSj8=', 0, 0, '2019-09-25 12:59:52'),
(7, 'claudiafield@email.com', 'V6VZhG5XYNEIrkYKjyWC4qwryuH9RvTy3sInvgICOLQ=', 'fTJIpNskivI8fppThze0HxjlMHgKcU6q1Ww5JWeqHqE=', 0, 0, '2019-09-25 13:13:15'),
(8, 'campbellmbutcher@gmail.com', 'VmVsdg3esEmyKQ40o2UpMOfJ2r8qon2ZnrLKDjI9nIQ=', 'UhbrEfRePfazFepvOu5pEvi4P6RkRu3U1I3t0gfIf5A=', 0, 0, '2019-09-25 13:22:26'),
(9, 'harrisoncook@yahoo.co.nz', '3URa8UqmpK7cW0f5RP3+iiyE/rUFzEWG9HICZV8NoRg=', 'HVE+Fn/6BGTr4ZrWu4aQMUDUwHHbKAMgLEVEcsorrSI=', 0, 0, '2019-09-25 13:27:46'),
(10, 'matthewminish@gmail.com', 'Hb8VuahiR+41BgQtARO7YKSY3j8aHhwRd4WmL26fcL4=', 'xSjgC7fTeFY3n8Qs5e2WmMXZ5LvrSIsrGmjxymSbW88=', 0, 0, '2019-09-25 13:34:41'),
(11, 'olliesharplin@email.com', 'gbCNmrwnXdR/Ze1vTvq2rX7yk/dcELQlHaxm8svexbY=', '+h2nYg7NihVM2TEA8e74W0x8u69havlBr3USOcLe3oY=', 0, 0, '2019-09-25 13:39:15');

-- Add sample data for TravellerTypeDefinitions
INSERT INTO TravellerTypeDefinition (description) VALUES ('Backpacker'), ('Luxury'), ('Functional/Business Traveller'), ('Groupies'), ('Thrillseeker'), ('Frequent Weekender'), ('Gap Year');

-- Add sample Profile
INSERT INTO Profile (user_id, first_name, middle_name, last_name, date_of_birth, gender, creation_date, profile_photo_guid, cover_photo_guid, deleted) VALUES
(1, 'Moffat', 'the', 'Proffat', '1990-01-01', 'Male', '2001-01-01 00:00:00', NULL, NULL, 0),
(2, 'Kermit', 'the', 'Frog', '1995-07-18', 'Female', '2002-01-02 00:00:00', NULL, NULL, 0),
(3, 'William', 'the', 'Conqueror', '1969-12-24', 'Other', '2003-01-03 00:00:00', NULL, NULL, 0),
(4, 'William', '', 'Wallace', '1997-12-23', 'Male', NULL, NULL, NULL, 0),
(5, 'Max', '', 'Andrew', '1999-05-05', 'Male', NULL, NULL, NULL, 0),
(6, 'Ryan', '', 'Chen', '1998-03-01', 'Male', NULL, NULL, NULL, 0),
(7, 'Claudia', '', 'Field', '1998-06-05', 'Female', NULL, NULL, NULL, 0),
(8, 'Campbell', '', 'Mercer-Butcher', '1998-10-07', 'Male', NULL, NULL, NULL, 0),
(9, 'Harrison', '', 'Cook', '1998-07-01', 'Male', NULL, NULL, NULL, 0),
(10, 'Matthew', '', 'Minish', '1997-04-25', 'Male', NULL, NULL, NULL, 0),
(11, 'Ollie', '', 'Sharplin', '1998-12-18', 'Male', NULL, NULL, NULL, 0);

INSERT INTO TravellerType (guid, user_id, traveller_type_id) VALUES
(1, 1, 1),
(2, 1, 3),
(3, 2, 2),
(4, 3, 4),
(5, 4, 1),
(6, 4, 7),
(7, 5, 3),
(8, 6, 5),
(9, 7, 4),
(10, 7, 6),
(11, 8, 1),
(12, 8, 5),
(13, 9, 2),
(14, 9, 3),
(15, 10, 3),
(16, 10, 6),
(17, 11, 1),
(18, 11, 4);

INSERT INTO Passport (guid, user_id, country_id) VALUES
(1, 1, 246),
(2, 4, 554),
(3, 5, 826),
(4, 6, 156),
(5, 8, 392),
(6, 9, 36),
(7, 10, 554),
(8, 11, 250);

INSERT INTO Nationality (guid, user_id, country_id) VALUES
(1, 1, 246),
(2, 1, 643),
(3, 2, 246),
(4, 3, 643),
(5, 4, 554),
(6, 5, 826),
(7, 6, 156),
(8, 6, 356),
(9, 7, 840),
(10, 8, 392),
(11, 9, 36),
(12, 10, 554),
(13, 11, 250);

-- Add sample data for destination
INSERT INTO Destination (id, user_id, name, type, district, latitude, longitude, country_id, is_public, deleted, primary_photo_guid) VALUES
(1, 1, 'Bangkok', 'City', 'Central Thailand', 13.7, 100.5, 764, 1, 0, NULL),
(2, 1, 'London', 'City', 'London Region', 51.3, 0, 826, 1, 0, NULL),
(3, 1, 'Paris', 'City', 'Île-de-France', 48.8, 2.4, 250, 1, 0, NULL),
(4, 1, 'Dubai', 'City', 'Dubai Region', 25.3, 55.3, 784, 1, 0, NULL),
(5, 1, 'Singapore', 'City', 'Central Region', 1.3, 103.8, 702, 1, 0, NULL),
(6, 1, 'New York City', 'City', 'New York', 40.6, -73.8, 840, 1, 0, NULL),
(7, 1, 'Kuala Lumpur', 'City', 'Selangor', 3.1, 101.6, 458, 1, 0, NULL),
(8, 1, 'Tokyo', 'City', 'Kanto', 35.6, 139.6, 392, 1, 0, NULL),
(9, 1, 'Istanbul', 'City', 'Marmara', 41, 28.9, 792, 1, 0, NULL),
(10, 1, 'Seoul', 'City', 'Seoul Capital Area', 37.5, 126.9, 410, 1, 0, NULL),
(11, 1, 'Phuket', 'City', 'Phuket Province', 8, 98.3, 764, 1, 0, NULL),
(12, 1, 'Mecca', 'City', 'Hejazi', 21.4, 39.8, 682, 1, 0, NULL),
(13, 1, 'Hong Kong', 'City', 'Southern China', 22.5, 114.3, 344, 1, 0, NULL),
(14, 1, 'San Antonia', 'City', 'Texas', 29.4, -98.5, 840, 1, 0, NULL),
(15, 1, 'Pueblo', 'City', 'East Central Mexico', 19, -97.9, 484, 1, 0, NULL),
(16, 1, 'Honolulu', 'City', 'Hawaii', 21.3, -157.8, 840, 1, 0, NULL),
(17, 1, 'Budapest', 'City', 'Central Hungary', 47.5, 19, 348, 1, 0, NULL),
(18, 1, 'Buenos Aires', 'City', 'Buenos Aires', -34.6, -58.4, 32, 1, 0, NULL),
(19, 1, 'Cape Town', 'City', 'Western Cape', -33.9, 18.4, 710, 1, 0, NULL),
(20, 1, 'Moscow', 'City', 'Central Russia', 55.8, 37.7, 643, 1, 0, NULL),
(21, 1, 'Taipei', 'City', 'Northern Taiwan', 25, 121.5, 158, 1, 0, NULL),
(22, 1, 'Sydney', 'City', 'New South Wales', -33.9, 151.1, 36, 1, 0, NULL),
(23, 1, 'Gold Coast', 'City', 'Queensland', -28, 153.4, 36, 1, 0, NULL),
(24, 2, 'Melbourne', 'City', 'Victoria', -37.8, 145, 36, 1, 0, NULL),
(25, 1, 'Perth', 'City', 'Western Australia', -32, 115.9, 36, 1, 0, NULL),
(26, 1, 'Brisbane', 'City', 'Queensland', -27.5, 153, 36, 1, 0, NULL),
(27, 1, 'Adelaide', 'City', 'South Australia', -34.9, 138.6, 36, 1, 0, NULL),
(28, 1, 'Auckland', 'City', 'Auckland', -36.9, 174.8, 554, 1, 0, NULL),
(29, 1, 'Tauranga', 'City', 'Bay of Plenty', -37.8, 176.1, 554, 1, 0, NULL),
(30, 1, 'Mount Maunganui', 'Mountain', 'Tauranga', -37.6, 176.2, 554, 1, 0, NULL),
(31, 1, 'Rotorua', 'City', 'Bay of Plenty', -38, 176.2, 554, 1, 0, NULL),
(32, 1, 'Wellington', 'City', 'Wellington', -41.3, 174.8, 554, 1, 0, NULL),
(33, 1, 'Christchurch', 'City', 'Canterbury', -43.5, 172.7, 554, 1, 0, NULL),
(34, 1, 'Dunedin', 'City', 'Otago', -45.9, 170.5, 554, 1, 0, NULL),
(35, 1, 'Wanaka', 'Town', 'Otago', -44.8, 169.1, 554, 1, 0, NULL),
(36, 1, 'Queenstown', 'Town', 'Otago', -45, 168.7, 554, 1, 0, NULL),
(37, 1, 'Hamilton', 'City', 'Waikato', -37.8, 175.3, 554, 1, 0, NULL),
(38, 1, 'Miami', 'City', 'Florida', 25.8, -80.1, 840, 1, 0, NULL),
(39, 1, 'Copenhagen', 'City', 'Copenhagen Region', 55.7, 12.5, 208, 1, 0, NULL),
(40, 1, 'Berlin', 'City', 'Brandenburg', 52.5, 13.4, 276, 1, 0, NULL),
(41, 1, 'Cairo', 'City', 'Cairo Region', 30, 31.2, 818, 1, 0, NULL),
(42, 1, 'Great Pyramid of Giza', 'Monument', 'Cairo Region', 30, 31.1, 818, 1, 0, NULL),
(43, 1, 'Grand Canyon', 'Canyon', 'Arizona', 36.3, -112.6, 840, 1, 0, NULL),
(44, 1, 'Venice', 'City', 'Veneto', 45.4, 12.3, 380, 1, 0, NULL),
(45, 1, 'Rome', 'City', 'Lazio', 41.9, 12.5, 380, 1, 0, NULL),
(46, 1, 'Prague', 'City', 'Prague Region', 50, 14.4, 203, 1, 0, NULL),
(47, 1, 'San Juan', 'City', 'San Juan Region', 18.4, -66, 630, 1, 0, NULL),
(48, 1, 'Dublin', 'City', 'Leinster', 53.4, -6.2, 826, 1, 0, NULL),
(49, 1, 'Toronto', 'City', 'Ontario', 43.7, -79.4, 124, 1, 0, NULL),
(50, 1, 'Rio de Janeiro', 'City', 'Rio de Janeiro', -25.9, -43.1, 76, 1, 0, NULL),
(51, 1, 'Las Vegas', 'City', 'Nevada', 36.1, -115.1, 840, 1, 0, NULL),
(52, 1, 'Eiffel Tower', 'Monument', 'Île-de-France', 48.8, 2.3, 250, 1, 0, NULL),
(53, 1, 'Hollywood Sign', 'Landmark', 'Los Angeles', 34.1, -118.3, 840, 1, 0, NULL),
(54, 1, 'Mount Fuji', 'Volcano', 'Honshu', 35.4, 138.7, 392, 1, 0, NULL),
(55, 1, 'Big Ben', 'Tower', 'London', 51.1, -0.2, 826, 1, 0, NULL),
(56, 1, 'Burj Khalifa', 'Skyscraper', 'Dubai', 25.1, 55.3, 784, 1, 0, NULL),
(57, 1, 'Colosseum', 'Monument', 'Rome', 41.9, 12.5, 380, 1, 0, NULL),
(58, 1, 'Times Square', 'Landmark', 'New York', 40.8, -74, 840, 1, 0, NULL),
(59, 1, 'Buckingham Palace', 'Palace', 'London', 51.5014, -0.1419, 826, 1, 0, NULL),
(60, 1, 'Leaning Tower of Pisa', 'Tower', 'Italy', 43.723, 10.3966, 380, 1, 0, NULL),
(61, 1, 'Forbidden City', 'Landmark', 'Beijing', 39.9169, 116.3907, 156, 1, 0, NULL),
(62, 1, 'Sydney Opera House', 'Landmark', 'Sydney', -33.8568, 151.2153, 36, 1, 0, NULL),
(63, 1, 'Mount Everest', 'Mountain', 'Himalayas', 27.9881, 86.925, 524, 1, 0, NULL),
(64, 1, 'Great Wall of China', 'Landmark', 'China', 40.4319, 116.5704, 156, 1, 0, NULL),
(65, 4, 'Barcelona', 'City', 'Spain', 41.434858975347744, 1.9817762790345341, 724, 1, 0, NULL),
(66, 4, 'Madrid', 'City', 'Spain', 40.40762773436398, -3.7169080094811306, 724, 1, 0, NULL),
(67, 4, 'Camp Nou', 'Stadium', 'Barcelona', 41.382022073192, 2.123699015441389, 4, 1, 0, NULL),
(68, 5, 'Mumbai', 'City', 'West India', 19.08717376176197, 72.91088490610548, 356, 1, 0, NULL),
(69, 5, 'Taj Mahal', 'Monument', 'India', 21, 78, 4, 1, 0, NULL),
(70, 6, 'Table Mountain', 'Mountain', 'Cape Town', -33.96016249731852, 18.40106964111328, 710, 1, 0, NULL),
(71, 4, 'New Delhi', 'City', 'North India', 28.523638120890865, 77.26730827319261, 356, 1, 0, NULL),
(72, 7, 'Manila', 'City', 'Manila', 14.58202788158964, 120.98786831224959, 608, 1, 0, NULL),
(73, 7, 'Cebu', 'City', 'Cebu', 10.317032141531492, 123.8776287731099, 608, 1, 0, NULL),
(74, 7, 'Siargoa Island', 'Island', 'Siargao', 9.84331303303718, 126.047302705128, 608, 1, 0, NULL),
(75, 9, 'Miami Beach', 'Beach', 'Miami', 25.829009515524376, -80.12093105198028, 840, 1, 0, NULL),
(76, 9, 'Athens', 'City', 'South Greece', 37.97418134892621, 23.715558681537345, 300, 1, 0, NULL),
(77, 11, 'Pyongyang', 'City', 'North Korea', 39.07236089431427, 125.77974523219882, 408, 1, 0, NULL),
(78, 11, 'Nairobi', 'City', 'Central Kenya', -1.239755720327607, 36.7345517821268, 404, 1, 0, NULL),
(79, 11, 'Sahara Desert', 'Desert', 'North Africa', 24.98098457315483, 11.85337769466912, 434, 1, 0, NULL);

-- Add traveller type to destination
INSERT INTO DestinationTravellerType (guid, dest_id, traveller_type_definition_id) VALUES
(1, 10, 3);

-- Add sample data for trip
INSERT INTO Trip (id, user_id, is_public, deleted, creation_date) VALUES
(1, 1, 1, 0, '2019-09-25 12:31:41'),
(2, 1, 1, 0, '2019-09-25 12:31:41'),
(3, 4, 1, 0, '2019-09-25 12:37:42'),
(4, 4, 1, 0, '2019-09-25 12:46:43'),
(5, 5, 1, 0, '2019-09-25 12:52:35'),
(6, 6, 1, 0, '2019-09-25 13:03:34'),
(7, 6, 1, 0, '2019-09-25 13:10:46'),
(8, 7, 1, 0, '2019-09-25 13:16:09'),
(9, 7, 1, 0, '2019-09-25 13:20:20'),
(10, 8, 1, 0, '2019-09-25 13:25:00'),
(11, 8, 1, 0, '2019-09-25 13:25:35'),
(12, 9, 1, 0, '2019-09-25 13:31:22'),
(13, 9, 1, 0, '2019-09-25 13:33:24'),
(14, 10, 1, 0, '2019-09-25 13:37:10'),
(15, 10, 1, 0, '2019-09-25 13:37:45'),
(16, 11, 1, 0, '2019-09-25 13:41:52');

-- Add sample tripData for the sample trip
INSERT INTO TripData (guid, trip_id, position, destination_id, arrival_time, departure_time) VALUES
(1, 1, 0, 1, NULL, NULL),
(2, 1, 1, 2, NULL, NULL),
(3, 1, 2, 3, NULL, NULL),
(4, 2, 0, 4, NULL, NULL),
(5, 2, 1, 5, NULL, NULL),
(6, 2, 2, 6, NULL, NULL),
(7, 3, 0, 28, NULL, '2019-10-23 12:00:00'),
(8, 3, 1, 18, '2019-10-23 23:30:00', '2019-10-26 09:15:00'),
(9, 3, 2, 50, '2019-10-26 13:45:00', '2019-11-05 15:30:00'),
(10, 4, 0, 28, NULL, NULL),
(11, 4, 1, 27, NULL, NULL),
(12, 4, 2, 22, NULL, NULL),
(13, 4, 3, 62, NULL, NULL),
(14, 4, 4, 26, NULL, NULL),
(15, 4, 5, 23, NULL, NULL),
(21, 5, 0, 48, NULL, NULL),
(22, 5, 1, 44, NULL, NULL),
(23, 5, 2, 60, NULL, NULL),
(24, 5, 3, 45, NULL, NULL),
(25, 5, 4, 57, NULL, NULL),
(26, 5, 5, 4, NULL, NULL),
(27, 5, 6, 56, NULL, NULL),
(28, 6, 0, 19, NULL, NULL),
(29, 6, 1, 70, NULL, NULL),
(36, 7, 0, 28, NULL, NULL),
(37, 7, 1, 27, NULL, NULL),
(38, 7, 2, 22, NULL, NULL),
(39, 7, 3, 62, NULL, NULL),
(40, 7, 4, 26, NULL, NULL),
(41, 7, 5, 23, NULL, NULL),
(42, 7, 6, 33, NULL, NULL),
(43, 8, 0, 43, NULL, NULL),
(44, 8, 1, 51, NULL, NULL),
(45, 8, 2, 38, NULL, NULL),
(46, 8, 3, 14, NULL, NULL),
(47, 8, 4, 6, NULL, NULL),
(48, 8, 5, 58, NULL, NULL),
(49, 9, 0, 72, NULL, NULL),
(50, 9, 1, 73, NULL, NULL),
(51, 9, 2, 74, NULL, NULL),
(52, 10, 0, 2, NULL, NULL),
(53, 10, 1, 55, NULL, NULL),
(54, 10, 2, 59, NULL, NULL),
(55, 11, 0, 33, NULL, NULL),
(56, 11, 1, 8, NULL, NULL),
(57, 11, 2, 54, NULL, NULL),
(58, 12, 0, 38, NULL, NULL),
(59, 12, 1, 75, NULL, NULL),
(60, 13, 0, 41, NULL, NULL),
(61, 13, 1, 42, NULL, NULL),
(62, 13, 2, 76, NULL, NULL),
(63, 14, 0, 28, NULL, NULL),
(64, 14, 1, 29, NULL, NULL),
(65, 14, 2, 30, NULL, NULL),
(66, 14, 3, 31, NULL, NULL),
(67, 14, 4, 37, NULL, NULL),
(68, 14, 5, 32, NULL, NULL),
(69, 15, 0, 33, NULL, NULL),
(70, 15, 1, 34, NULL, NULL),
(71, 15, 2, 35, NULL, NULL),
(72, 15, 3, 36, NULL, NULL),
(73, 16, 0, 10, NULL, NULL),
(74, 16, 1, 77, NULL, NULL);

-- Add sample tags
INSERT INTO Tag (id, name) VALUES
(3, '#TravelEA'),
(21, 'Africa'),
(24, 'america'),
(5, 'Argentina'),
(13, 'Australia'),
(32, 'beach'),
(7, 'Brazil'),
(40, 'dangerous'),
(17, 'dubai'),
(33, 'egypt'),
(39, 'exciting'),
(11, 'Footbal'),
(34, 'greece'),
(12, 'holiday'),
(8, 'Hot'),
(18, 'india'),
(27, 'island'),
(15, 'italy'),
(30, 'japan'),
(38, 'korea'),
(19, 'landmark'),
(29, 'london'),
(31, 'miami'),
(20, 'Mountain'),
(35, 'North Island'),
(37, 'north korea'),
(25, 'philippines'),
(6, 'Rio'),
(23, 'road trip'),
(1, 'Russia'),
(10, 'Soccer'),
(4, 'South America'),
(36, 'South Island'),
(9, 'Spain'),
(2, 'sports'),
(14, 'sun'),
(16, 'travel'),
(26, 'tropical');

INSERT INTO DestinationTag (guid, tag_id, destination_id) VALUES
(1, 2, 1),
(2, 1, 1),
(3, 8, 65),
(4, 9, 65),
(5, 9, 66),
(6, 9, 67),
(7, 10, 67),
(8, 11, 67),
(9, 18, 68),
(10, 18, 69),
(11, 19, 69),
(12, 19, 70),
(13, 20, 70),
(14, 18, 71),
(19, 25, 73),
(20, 27, 74),
(21, 25, 74),
(22, 26, 74),
(23, 31, 75),
(24, 32, 75),
(25, 37, 77);

INSERT INTO TripTag (guid, tag_id, trip_id) VALUES
(1, 3, 1),
(2, 4, 3),
(3, 5, 3),
(4, 6, 3),
(5, 7, 3),
(6, 12, 4),
(7, 13, 4),
(8, 14, 4),
(11, 15, 5),
(12, 16, 5),
(13, 17, 5),
(14, 21, 6),
(15, 23, 8),
(16, 24, 8),
(18, 29, 10),
(19, 30, 11),
(20, 32, 12),
(21, 33, 13),
(22, 34, 13),
(23, 35, 14),
(24, 36, 15),
(25, 38, 16),
(26, 39, 16),
(27, 40, 16);

INSERT INTO UsedTag (guid, tag_id, user_id, time_used) VALUES
(1, 3, 1, '2019-09-25 12:31:41'),
(2, 2, 1, '2019-09-25 12:31:41'),
(3, 1, 1, '2019-09-25 12:31:41'),
(4, 2, 2, '2019-09-25 12:31:41'),
(5, 4, 4, NULL),
(6, 5, 4, NULL),
(7, 6, 4, NULL),
(8, 7, 4, NULL),
(9, 8, 4, NULL),
(10, 9, 4, '2019-09-25 12:45:44'),
(11, 10, 4, NULL),
(12, 11, 4, NULL),
(13, 12, 4, NULL),
(14, 13, 4, NULL),
(15, 14, 4, NULL),
(16, 15, 5, NULL),
(17, 16, 5, NULL),
(18, 17, 5, NULL),
(19, 18, 5, '2019-09-25 12:55:51'),
(20, 19, 5, NULL),
(21, 19, 6, NULL),
(22, 20, 6, NULL),
(23, 21, 6, NULL),
(24, 18, 4, NULL),
(25, 23, 7, NULL),
(26, 24, 7, NULL),
(27, 25, 7, '2019-09-25 13:20:20'),
(28, 26, 7, NULL),
(29, 27, 7, NULL),
(30, 29, 8, NULL),
(31, 30, 8, NULL),
(32, 31, 9, NULL),
(33, 32, 9, '2019-09-25 13:31:22'),
(34, 33, 9, NULL),
(35, 34, 9, NULL),
(36, 35, 10, NULL),
(37, 36, 10, NULL),
(38, 37, 11, NULL),
(39, 38, 11, NULL),
(40, 39, 11, NULL),
(41, 40, 11, NULL);

INSERT INTO FollowerDestination (guid, destination_id, follower_id, follow_time, deleted) VALUES
(1, 65, 4, '2019-09-25 12:51:11', 0),
(2, 28, 4, '2019-09-25 12:51:18', 0),
(3, 68, 5, '2019-09-25 12:56:31', 0),
(4, 69, 5, '2019-09-25 12:56:34', 0),
(5, 48, 5, '2019-09-25 12:56:44', 0),
(6, 45, 5, '2019-09-25 12:56:54', 0),
(7, 70, 4, '2019-09-25 13:11:58', 0),
(8, 70, 6, '2019-09-25 13:12:13', 0),
(9, 28, 6, '2019-09-25 13:12:31', 0),
(10, 28, 7, '2019-09-25 13:14:47', 0),
(11, 70, 7, '2019-09-25 13:15:12', 0),
(12, 74, 7, '2019-09-25 13:21:17', 0),
(13, 73, 7, '2019-09-25 13:21:25', 0),
(14, 72, 7, '2019-09-25 13:21:29', 0),
(15, 28, 8, '2019-09-25 13:24:12', 0),
(16, 1, 8, '2019-09-25 13:24:24', 0),
(17, 69, 8, '2019-09-25 13:26:01', 0),
(18, 18, 8, '2019-09-25 13:26:37', 0),
(19, 1, 6, '2019-09-25 13:26:44', 0),
(20, 18, 6, '2019-09-25 13:26:50', 0),
(21, 69, 9, '2019-09-25 13:29:41', 0),
(22, 1, 9, '2019-09-25 13:29:48', 0),
(23, 75, 9, '2019-09-25 13:31:48', 0),
(24, 38, 9, '2019-09-25 13:32:00', 0),
(25, 76, 9, '2019-09-25 13:34:02', 0),
(26, 1, 10, '2019-09-25 13:36:05', 0),
(27, 5, 10, '2019-09-25 13:36:18', 0),
(28, 29, 10, '2019-09-25 13:38:18', 0),
(29, 36, 10, '2019-09-25 13:38:30', 0),
(30, 6, 10, '2019-09-25 13:38:44', 0),
(31, 38, 10, '2019-09-25 13:38:50', 0),
(32, 77, 11, '2019-09-25 13:42:00', 0);

INSERT INTO FollowerUser (guid, user_id, follower_id, follow_time, deleted) VALUES
(1, 1, 4, '2019-09-25 12:38:55', 0),
(2, 2, 4, '2019-09-25 12:38:58', 0),
(3, 3, 4, '2019-09-25 12:39:01', 0),
(4, 4, 5, '2019-09-25 12:50:27', 0),
(5, 1, 5, '2019-09-25 12:57:04', 0),
(6, 2, 5, '2019-09-25 12:57:09', 0),
(7, 3, 5, '2019-09-25 12:57:12', 0),
(8, 5, 4, '2019-09-25 12:57:24', 0),
(9, 4, 6, '2019-09-25 13:01:05', 0),
(10, 5, 6, '2019-09-25 13:01:39', 0),
(11, 1, 6, '2019-09-25 13:01:43', 0),
(12, 6, 4, '2019-09-25 13:09:15', 0),
(13, 2, 6, '2019-09-25 13:12:18', 0),
(14, 4, 7, '2019-09-25 13:14:03', 0),
(15, 6, 7, '2019-09-25 13:14:11', 0),
(16, 5, 7, '2019-09-25 13:14:19', 0),
(17, 7, 6, '2019-09-25 13:20:59', 0),
(18, 8, 6, '2019-09-25 13:23:15', 0),
(19, 6, 8, '2019-09-25 13:23:50', 0),
(20, 7, 8, '2019-09-25 13:25:47', 0),
(21, 4, 8, '2019-09-25 13:25:50', 0),
(22, 5, 8, '2019-09-25 13:25:57', 0),
(23, 1, 8, '2019-09-25 13:26:13', 0),
(24, 7, 9, '2019-09-25 13:28:49', 0),
(25, 4, 9, '2019-09-25 13:29:17', 0),
(26, 6, 9, '2019-09-25 13:29:21', 0),
(27, 8, 9, '2019-09-25 13:29:26', 0),
(28, 5, 9, '2019-09-25 13:29:29', 0),
(29, 9, 8, '2019-09-25 13:31:33', 0),
(30, 7, 10, '2019-09-25 13:35:24', 0),
(31, 4, 10, '2019-09-25 13:35:27', 0),
(32, 9, 10, '2019-09-25 13:35:31', 0),
(33, 6, 10, '2019-09-25 13:35:34', 0),
(34, 8, 10, '2019-09-25 13:35:38', 0),
(35, 5, 10, '2019-09-25 13:35:41', 0),
(36, 10, 9, '2019-09-25 13:38:00', 0),
(37, 9, 11, '2019-09-25 13:39:56', 0),
(38, 6, 11, '2019-09-25 13:39:59', 0),
(39, 8, 11, '2019-09-25 13:40:02', 0),
(40, 5, 11, '2019-09-25 13:40:05', 0),
(41, 10, 11, '2019-09-25 13:40:09', 0),
(42, 7, 11, '2019-09-25 13:40:13', 0),
(43, 4, 11, '2019-09-25 13:40:18', 0),
(44, 11, 9, '2019-09-25 13:43:22', 0),
(45, 10, 5, '2019-09-25 13:46:10', 0),
(46, 7, 5, '2019-09-25 13:46:14', 0),
(47, 9, 5, '2019-09-25 13:46:17', 0);

INSERT INTO NewsFeedEvent (guid, event_type, user_id, dest_id, ref_id, created) VALUES
(1, 'CREATED_NEW_TRIP', 4, NULL, 3, '2019-09-25 12:37:42'),
(2, 'CREATED_NEW_DESTINATION', 4, NULL, 65, '2019-09-25 12:38:21'),
(3, 'CREATED_NEW_DESTINATION', 4, NULL, 66, '2019-09-25 12:43:30'),
(4, 'CREATED_NEW_DESTINATION', 4, NULL, 67, '2019-09-25 12:45:44'),
(5, 'CREATED_NEW_TRIP', 4, NULL, 4, '2019-09-25 12:46:43'),
(6, 'CREATED_NEW_TRIP', 5, NULL, 5, '2019-09-25 12:52:35'),
(7, 'UPDATED_EXISTING_TRIP', 5, 56, 5, '2019-09-25 12:53:28'),
(8, 'UPDATED_EXISTING_TRIP', 5, 4, 5, '2019-09-25 12:53:28'),
(9, 'CREATED_NEW_DESTINATION', 5, NULL, 68, '2019-09-25 12:54:21'),
(10, 'CREATED_NEW_DESTINATION', 5, NULL, 69, '2019-09-25 12:55:51'),
(11, 'CREATED_NEW_DESTINATION', 6, NULL, 70, '2019-09-25 13:03:00'),
(12, 'CREATED_NEW_TRIP', 6, NULL, 6, '2019-09-25 13:03:34'),
(13, 'CREATED_NEW_DESTINATION', 4, NULL, 71, '2019-09-25 13:10:23'),
(14, 'CREATED_NEW_TRIP', 6, NULL, 7, '2019-09-25 13:11:31'),
(15, 'CREATED_NEW_TRIP', 7, NULL, 8, '2019-09-25 13:16:09'),
(16, 'CREATED_NEW_DESTINATION', 7, NULL, 72, '2019-09-25 13:16:44'),
(17, 'CREATED_NEW_DESTINATION', 7, NULL, 73, '2019-09-25 13:17:12'),
(18, 'CREATED_NEW_DESTINATION', 7, NULL, 74, '2019-09-25 13:18:17'),
(19, 'UPDATED_EXISTING_DESTINATION', 7, NULL, 73, '2019-09-25 13:19:48'),
(20, 'UPDATED_EXISTING_DESTINATION', 7, NULL, 74, '2019-09-25 13:20:04'),
(21, 'CREATED_NEW_TRIP', 7, NULL, 9, '2019-09-25 13:20:31'),
(22, 'CREATED_NEW_TRIP', 8, NULL, 10, '2019-09-25 13:25:00'),
(23, 'CREATED_NEW_TRIP', 8, NULL, 11, '2019-09-25 13:25:35'),
(24, 'CREATED_NEW_DESTINATION', 9, NULL, 75, '2019-09-25 13:31:07'),
(25, 'CREATED_NEW_TRIP', 9, NULL, 12, '2019-09-25 13:31:22'),
(26, 'CREATED_NEW_DESTINATION', 9, NULL, 76, '2019-09-25 13:32:51'),
(27, 'CREATED_NEW_TRIP', 9, NULL, 13, '2019-09-25 13:33:24'),
(28, 'CREATED_NEW_TRIP', 10, NULL, 14, '2019-09-25 13:37:10'),
(29, 'CREATED_NEW_TRIP', 10, NULL, 15, '2019-09-25 13:37:45'),
(30, 'CREATED_NEW_DESTINATION', 11, NULL, 77, '2019-09-25 13:41:23'),
(31, 'CREATED_NEW_TRIP', 11, NULL, 16, '2019-09-25 13:41:52'),
(32, 'CREATED_NEW_DESTINATION', 11, NULL, 78, '2019-09-25 13:44:32'),
(33, 'CREATED_NEW_DESTINATION', 11, NULL, 79, '2019-09-25 13:45:25');

INSERT INTO Likes (guid, event_id, user_id, deleted) VALUES
(1, 5, 4, 0),
(2, 10, 4, 0),
(3, 9, 4, 0),
(4, 7, 4, 0),
(5, 6, 4, 0),
(6, 5, 5, 0),
(7, 2, 5, 0),
(8, 4, 5, 0),
(9, 5, 6, 0),
(10, 12, 4, 0),
(11, 14, 6, 0),
(12, 14, 4, 0),
(13, 10, 7, 0),
(14, 5, 7, 0),
(15, 13, 7, 0),
(16, 21, 7, 0),
(17, 21, 6, 0),
(18, 15, 7, 0),
(19, 14, 8, 0),
(20, 12, 8, 0),
(21, 23, 8, 0),
(22, 10, 8, 0),
(23, 21, 9, 0),
(24, 19, 9, 0),
(25, 15, 9, 0),
(26, 13, 9, 0),
(27, 5, 9, 0),
(28, 4, 9, 0),
(29, 25, 9, 0),
(30, 25, 8, 0),
(31, 27, 9, 0),
(32, 27, 8, 0),
(33, 26, 8, 0),
(34, 27, 11, 0),
(35, 25, 11, 0),
(36, 26, 11, 0),
(37, 24, 11, 0),
(38, 14, 11, 0),
(39, 11, 11, 0),
(40, 23, 11, 0),
(41, 10, 11, 0),
(42, 7, 11, 0),
(43, 6, 11, 0),
(44, 29, 11, 0),
(45, 21, 11, 0),
(46, 18, 11, 0),
(47, 13, 11, 0),
(48, 5, 11, 0),
(49, 4, 11, 0),
(50, 3, 11, 0),
(51, 31, 11, 0),
(52, 31, 9, 0),
(53, 33, 11, 0),
(54, 10, 5, 0),
(55, 13, 5, 0);

-- !Downs
DELETE FROM UsedTag;
DELETE FROM PhotoTag;
DELETE FROM TripTag;
DELETE FROM DestinationTag;
DELETE FROM Tag;
DELETE FROM TreasureHunt;
DELETE FROM PendingDestinationPhoto;
DELETE FROM DestinationPhoto;
DELETE FROM TripData;
DELETE FROM Trip;
DELETE FROM DestinationTravellerTypePending;
DELETE FROM DestinationTravellerType;
DELETE FROM Destination;
DELETE FROM TravellerType;
DELETE FROM TravellerTypeDefinition;
DELETE FROM Passport;
DELETE FROM Nationality;
DELETE FROM CountryDefinition;
DELETE FROM Profile;
DELETE FROM Photo;
DELETE FROM User;