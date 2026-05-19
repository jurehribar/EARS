% MATLAB script to plot the SpherePlateau function
% f(x,y) = 144 if any x_i in [10, 17]
%        = x^2 + y^2 otherwise
%
% This is a modified Sphere function with a plateau region

% Define the domain
x = linspace(-20, 30, 500);
y = linspace(-20, 30, 500);
[X, Y] = meshgrid(x, y);

% Initialize Z matrix
Z = zeros(size(X));

% Calculate the SpherePlateau function
for i = 1:length(x)
    for j = 1:length(y)
        xval = X(i,j);
        yval = Y(i,j);

        % Check if either coordinate is in the plateau region [10, 17]
        if (xval >= 10 && xval <= 17) || (yval >= 10 && yval <= 17)
            Z(i,j) = 144;
        else
            Z(i,j) = xval^2 + yval^2;
        end
    end
end

% Create the 3D surface plot
figure('Position', [100, 100, 1200, 500]);

% Subplot 1: 3D surface
subplot(1, 2, 1);
surf(X, Y, Z, 'EdgeColor', 'none');
colormap(jet);
colorbar;
xlabel('x');
ylabel('y');
zlabel('f(x,y)');
title('SpherePlateau Function - 3D View');
view(-30, 30);
grid on;
axis tight;

% Mark the global optimum at origin
hold on;
plot3(0, 0, 0, 'y*', 'MarkerSize', 20, 'LineWidth', 2);
% Draw the plateau boundaries
plateau_x = [10, 17, 17, 10, 10];
plateau_y = [10, 10, 17, 17, 10];
plateau_z = 144 * ones(1, 5);
plot3(plateau_x, plateau_y, plateau_z, 'r-', 'LineWidth', 2);
hold off;

% Subplot 2: Contour plot
subplot(1, 2, 2);
contourf(X, Y, Z, 30);
colormap(jet);
colorbar;
xlabel('x');
ylabel('y');
title('SpherePlateau Function - Contour Plot');
grid on;
axis equal;
xlim([-20 30]);
ylim([-20 30]);

% Mark important features
hold on;
% Mark the global optimum
plot(0, 0, 'y*', 'MarkerSize', 20, 'LineWidth', 2);
% Draw rectangle showing plateau region
rectangle('Position', [10, 10, 7, 7], 'EdgeColor', 'r', 'LineWidth', 2, 'LineStyle', '--');
% Add cross-shaped plateau region (since it's OR condition)
plot([10 10], [-20 30], 'r--', 'LineWidth', 1.5);
plot([17 17], [-20 30], 'r--', 'LineWidth', 1.5);
plot([-20 30], [10 10], 'r--', 'LineWidth', 1.5);
plot([-20 30], [17 17], 'r--', 'LineWidth', 1.5);
legend('', '', 'Global Optimum (0,0)', 'Plateau Boundary', 'Location', 'northeast');
hold off;

% Create a third figure showing cross-sections
figure('Position', [100, 650, 1200, 400]);

% Subplot 1: Cross-section along x-axis (y=0)
subplot(1, 2, 1);
x_vals = linspace(-20, 30, 1000);
f_x = zeros(size(x_vals));
for i = 1:length(x_vals)
    if x_vals(i) >= 10 && x_vals(i) <= 17
        f_x(i) = 144;
    else
        f_x(i) = x_vals(i)^2;
    end
end
plot(x_vals, f_x, 'b-', 'LineWidth', 2);
grid on;
xlabel('x (with y = 0)');
ylabel('f(x, 0)');
title('Cross-Section Along x-axis (y = 0)');
hold on;
plot(0, 0, 'ro', 'MarkerSize', 10, 'LineWidth', 2, 'MarkerFaceColor', 'r');
% Mark plateau region
plot([10 17], [144 144], 'r-', 'LineWidth', 4);
xline(10, 'r--', 'LineWidth', 1);
xline(17, 'r--', 'LineWidth', 1);
legend('f(x, 0)', 'Global minimum', 'Plateau', 'Location', 'north');
hold off;

% Subplot 2: Cross-section along y-axis (x=0)
subplot(1, 2, 2);
y_vals = linspace(-20, 30, 1000);
f_y = zeros(size(y_vals));
for i = 1:length(y_vals)
    if y_vals(i) >= 10 && y_vals(i) <= 17
        f_y(i) = 144;
    else
        f_y(i) = y_vals(i)^2;
    end
end
plot(y_vals, f_y, 'b-', 'LineWidth', 2);
grid on;
xlabel('y (with x = 0)');
ylabel('f(0, y)');
title('Cross-Section Along y-axis (x = 0)');
hold on;
plot(0, 0, 'ro', 'MarkerSize', 10, 'LineWidth', 2, 'MarkerFaceColor', 'r');
% Mark plateau region
plot([10 17], [144 144], 'r-', 'LineWidth', 4);
xline(10, 'r--', 'LineWidth', 1);
xline(17, 'r--', 'LineWidth', 1);
legend('f(0, y)', 'Global minimum', 'Plateau', 'Location', 'north');
hold off;

% Print some information
fprintf('SpherePlateau Function Properties:\n');
fprintf('Function: f(x,y) = 144 if (x in [10,17] OR y in [10,17])\n');
fprintf('                 = x^2 + y^2 otherwise\n');
fprintf('Domain: x, y in [-100, 100] (shown [-20, 30])\n');
fprintf('Global optimum: (0, 0) with f(0, 0) = 0\n');
fprintf('Plateau value: 144 (= 12^2, where 12 is midpoint of [10, 17])\n');
fprintf('Plateau region: Cross-shaped region where x in [10,17] OR y in [10,17]\n');
fprintf('\nProperties:\n');
fprintf('  - Unimodal (single global minimum)\n');
fprintf('  - Discontinuous at plateau boundaries\n');
fprintf('  - Flat plateau at height 144\n');
fprintf('  - Based on Sphere function with modified region\n');
fprintf('  - Tests algorithm behavior with flat regions\n');
fprintf('  - Gradient is zero on the plateau\n');
fprintf('\nValues at key points:\n');
fprintf('  f(0, 0) = 0 (global minimum)\n');
fprintf('  f(10, 0) = 100 (edge of plateau, but y=0 so still sphere)\n');
fprintf('  f(0, 10) = 100 (edge of plateau, but x=0 so still sphere)\n');
fprintf('  f(10, 10) = 144 (inside plateau)\n');
fprintf('  f(13.5, 13.5) = 144 (center of plateau)\n');
fprintf('  f(17, 17) = 144 (other corner of plateau)\n');
fprintf('  f(20, 20) = 800 (outside plateau)\n');

