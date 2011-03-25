function [ ] = updateCars( carlist, dTime, edgeInfo)
%UPDATECARS moves all cars around according to the IDM
%   carlist: array of cars that need to be updated 
%   (---all the cars on 1 edge preferably---)
%   dTime: time in seconds that everything will be moved
%           this needs to be adjusted to our liking (accuracy vs. time
%           spend modelling)
%   edgeInfo: info about the edge the cars are on (like length


% First sort the cars (don't know in what way the array is delivered)
% we also need a leader for IDM
cars = qsort(carlist);

leader = cars(1,:); 
% atm we use the leader like in the paper but this can lead to wrong
% behavior when using multiple-lanes / having large gaps / multiple queues
% TODO: find solution for leader-problem!

% for each car the position of the car is changed
% 
for i = 1:size(cars,1)
    if i ~= 1
        s = cars(i-1,3) - cars(i,3);
    else
        s = 10000;
    end
    v = cars(i,7);
    dV = (leader(7) - v); % we assume, they assumed that the leader went the fastest
    pos = cars(i,3); 
    axel = idm(edgeInfo, cars(i,:), s, v, dV);
    cars(i,7) = cars(i,7) + (axel .* dTime);
    pos = pos + (cars(i,7) .* dTime);
    
% TODO: need a solution for nodes, when the cars are queuing up for the
%       node they need to brake at the ending and know they are at a
%       junction
    if(pos > (edgeInfo(6) - 3))
        %move to the other edge
        % EXPERIMENTAL, TODO: proper node handleage, yes handleage
        
    end
end

% IDM PART, see "Driver model & lane changing" paper - page 31
    function accel = idm(edge, car, s, v, dV)
        a = car_types(car(4),4);
        % atm everyone driver as fast as possible
        % TODO: add driver_types to have old grannys and speed-pirates!
        % huzzay
        if car_types(car(4),3) < edge(5)
            vo = car_types(car(4),3);
        else
            vo = edge(5);
        end
        accel = a .* (1 - (v/vo)^4 - (desDis(v, dV, a)/s)^2);
    end
    function dis = desDis(v , dV, a)
        so = 2; % 2 meters minimum distance to leader (see paper)
        T = 2; % like in the safety-ads 2 seconds distance between 2 cars
        b = 2; % max deceleration-rate
        dis = so + v .* T + ((v .* dV)/(2 .* (sqrt(a .* b))));
    end
    
%   function needed for sorting all the cars on a lane 
%   TODO: Multiple lanes!
    function sortedCars = qsort(cars)
        less = [];
        more = [];
        if size(cars,1) < 2
            sortedCars = cars;
        else
            pivotIndex = fix(size(cars,1)/2);
            pivot = cars(pivotIndex,:);
            for i = 1:size(cars,1)
                if cars(i,3) <= pivot(3)
                    less = [less;cars(i,:)];
                else
                    more = [more;cars(i,:)];
                end
               
            end
            sortedCars = [qsort(more);pivot; qsort(less)];
        end
        
    end

end
