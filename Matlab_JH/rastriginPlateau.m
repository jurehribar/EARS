function Z = rastriginPlateau(X, Y, plateauSize)
%RASTRIGINPLATEAU  2D Rastrigin function with square extrema plateaus.
%
%   Z = rastriginPlateau(X, Y) evaluates a 2D Rastrigin-based function.
%   Small 0.1 x 0.1 square plateaus are placed around the approximate local
%   minima at integer coordinates and the approximate local maxima at
%   half-integer coordinates.
%
%   Z = rastriginPlateau(X, Y, plateauSize) uses a custom square plateau
%   width. X and Y can be scalars, vectors, or matrices of matching size.

if nargin < 3
    plateauSize = 0.1;
end

halfWidth = plateauSize / 2;

Z = rastrigin2d(X, Y);

minX = round(X);
minY = round(Y);
onMinPlateau = abs(X - minX) <= halfWidth & abs(Y - minY) <= halfWidth;
Z(onMinPlateau) = rastrigin2d(minX(onMinPlateau), minY(onMinPlateau));

maxX = floor(X) + 0.5;
maxY = floor(Y) + 0.5;
onMaxPlateau = abs(X - maxX) <= halfWidth & abs(Y - maxY) <= halfWidth;
Z(onMaxPlateau) = rastrigin2d(maxX(onMaxPlateau), maxY(onMaxPlateau));

end

function Z = rastrigin2d(X, Y)
Z = 20 + X.^2 + Y.^2 ...
    - 10 .* cos(2 .* pi .* X) ...
    - 10 .* cos(2 .* pi .* Y);
end
