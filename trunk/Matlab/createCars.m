function [ cars ] = createCars( car_count )
%CREATECAR Summary of this function goes here
%   Detailed explanation goes here

cars = [0,0,0,0,0,0,0];

for i=1:1:car_count,
   s = size(cars);
   cars = [cars;[i,0,0,0,0,0,i]];
    end
end

