function [ cars ] = createCars( car_count )
%CREATECAR Summary of this function goes here
%   Detailed explanation goes here
%   See matlabDataFiles.txt (we count from 1 to n)
cars = [];

for i=1:1:car_count,
   s = size(cars);
   cars = [cars;[i,0,0,0,0,0,i,0]];
    end
end

