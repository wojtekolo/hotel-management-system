INSERT INTO room_type (capacity, description, name, price_per_night)
VALUES (2, 'Default room type description', 'Default room type',100.0)
ON CONFLICT (name) DO NOTHING;

INSERT INTO room(name,floor,room_type_id)
VALUES (001,1,1)
ON CONFLICT (name) DO NOTHING;

INSERT INTO room(name,floor,room_type_id)
VALUES (002,1,1)
ON CONFLICT (name) DO NOTHING;

INSERT INTO person(birth_date, name, surname)
VALUES ('2000-10-10','Customer_name','Customer_surname');

INSERT INTO person(birth_date, name, surname)
VALUES ('2000-10-10','Employee_name','Employee_surname');

INSERT INTO loyalty_status(discount,name)
VALUES(0.0,'BASIC')
ON CONFLICT (name) DO NOTHING;

INSERT INTO customer (id,private_email,private_phone,loyalty_status_id)
values (1,'customer@email.com', '123123123', 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO employee (id,employee_role,hire_date,id_card_number,pesel,work_email,work_phone)
values (2,'CASHIER','2020-10-10', '1231','12345678912',
        'employee@email.com', '111222333')
ON CONFLICT (id) DO NOTHING;
