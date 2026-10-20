create database JDBC_Projects;
use JDBC_Projects;

-- Student Management System
create table students(student_id int primary key auto_increment, student_name varchar(25) not null, course varchar(25) not null,
						email varchar(30) not null, marks decimal(10,2) default 0);
              
-- Library Management System              
create table books(book_id int primary key auto_increment, title varchar(25) not null, author varchar(25), 
					available boolean default true);
                    
create table members(member_id int primary key auto_increment, member_name varchar(25) not null);

create table book_issues(issue_id int primary key auto_increment, book_id int not null, member_id int not null, 
							issue_date date not null,return_date date, foreign key (book_id) references books(book_id), 
                            foreign key (member_id) references members(member_id));