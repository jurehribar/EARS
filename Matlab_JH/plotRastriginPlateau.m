clear; close all; clc;

plateauSize = 0.1;
domain = -5.12:0.01:5.12;
[X, Y] = meshgrid(domain, domain);
Z = rastriginPlateau(X, Y, plateauSize);

figure('Color', 'w');
surf(X, Y, Z, 'EdgeColor', 'none');
colormap turbo;
colorbar;
view(45, 55);
xlabel('x');
ylabel('y');
zlabel('f(x, y)');
title(sprintf('Rastrigin with %.2f x %.2f extrema plateaus', ...
    plateauSize, plateauSize));
axis tight;
camlight headlight;
lighting gouraud;

figure('Color', 'w');
contourf(X, Y, Z, 60, 'LineColor', 'none');
colormap turbo;
colorbar;
axis equal tight;
xlabel('x');
ylabel('y');
title(sprintf('Top view: %.2f x %.2f plateaus', ...
    plateauSize, plateauSize));
