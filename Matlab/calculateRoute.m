function [ route ] = calculateRoute( startnode, endnode, nodes, edges, destinations, car_types )
%calculateRoute calculates the quickest route from A to B
%   Its an A-Star, bitch!
opennodes = [ startnode ];

nodevalues = [,,]

closednodes = [];

currentnode = 0;

while (size(opennodes,1) > 0)
    for n = opennodes
        if  (
                (currentnode == 0) || 
                (nodevalues(n,1) < nodevalues(currentnode,1)) ||
                (
                    nodevalues(n,1) == nodevalues(currentnode,1) && 
                    nodevalues(n,3) < nodesvalues(currentnode,3)
                )
            )
            currentnode = n;
            break;
        end
    end
end

end

