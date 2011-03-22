%Clear our screen and clear all vars
clc
clear

% lets make up some car types
% [0] car-type id
% [1] length of vehicle in m
% [2] top speed of vehicle in m/s
car_types = [
    1       18.0    22.2; % 18 metres long, top speed 80
    2       4.0     33.3; % 4 metres long, top speed 120
    3       4.0     44.4 % 4 metres long, top speed 160
    ]

% Here we set global vars so we can easily config the simulation.
car_count = 1000;

%create the cars
cars = createCars(car_count);

% TODO import/make up a small road network from csv/xml/whatever.
% some nodes.
% [0] node id
% [1] metric x-coordinate
% [2] metric y-coordinate
nodes = [
    1       20      20;
    2       40      40;
    3       60      60;
    4       20      40
    ]

% some edges that correspond with the nodes.
% [0] edge id
% [1] lanes
% [2] startnode
% [3] endnode
% [4] maximum speed in m/s
% [5] length in m
% [6] minimum travel time (length / maximum speed) 
edges = [
    1   1   1   2   30.0    15.0    (15.0/30.0);
    2   1   2   1   40.0    17.0    (17.0/40.0);
    3   1   2   3   35.0    23.0    (23.0/35.0);
    4   1   3   2   25.0    50.0    (50.0/25.0);
    5   1   3   4   20.0    15.0    (15.0/20.0);
    6   1   4   3   50.0    35.0    (35.0/50.0);
    7   1   4   2   40.0    20.0    (20.0/40.0);
    8   1   2   4   30.0    45.0    (45.0/30.0);
    ]

% this var will contain the destinations of all nodes.
% we are using a cell array here, they seem to be reasonably fast =)
% [0] node id
% [1] edges that can be visited from that node

destinations = cell(size(nodes,1),1);

destinations{1} = [1];
destinations{2} = [2 3 8];
destinations{3} = [4 5];
destinations{4} = [6 7 8];

destinations

% DEMO CODE this will return all edges accessible from node 2.
destinations{2}

% DEMO CODE this changes the values of a row
% destinations{2} = [3,4,8];

% Routes
% a route is a list of nodes (or edges???) to follow
% [0] route id
% [1] vector of nodes to visit left to right

