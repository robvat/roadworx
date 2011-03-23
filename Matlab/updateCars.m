function [ ] = updateCars( cars, dTime )
%UPDATECARS moves all cars around according to te IDM
%   cars: array of cars that need to be updated 
%   (---all the cars on 1 edge preferably---)
%   dTime: time in millesecond that everything will be moved
%           this needs to be adjusted to our liking (accuracy vs. time
%           spend modelling)


% for each car the position of the car is changed
% 
for i = 1:size(cars,1)
dis = idm(s, v, dV) .* dTime^2;
pos = pos + dis;
end

% IDM PART, see "Driver model & lane changing" paper - page 31
    function accel = idm(s, v, dV)
        accel = a .* (1 - (v/vo)^4 - (desDis(v, dV)/s)^2);
    end
    function dis = desDis(v , dV)
        dis = so + v .* T + ((v .* dV)/(2 .* (sqrt(a .* b))));
    end
end
