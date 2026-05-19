% MATLAB script to plot the Sphere function
% f(x,y) = x^2 + y^2
%
% This is one of the simplest benchmark functions in optimization
% Creates a smooth paraboloid (bowl shape) with a single global minimum

% Define the domain
x = linspace(-10, 10, 500);
y = linspace(-10, 10, 500);
[X, Y] = meshgrid(x, y);

% Calculate the Sphere function
Z = X.^2 + Y.^2;

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
title('Sphere Function - 3D View');
view(-30, 30);
grid on;
axis tight;

% Mark the global optimum at origin
hold on;
plot3(0, 0, 0, 'y*', 'MarkerSize', 20, 'LineWidth', 2);
hold off;

% Subplot 2: Contour plot
subplot(1, 2, 2);
contourf(X, Y, Z, 30);
colormap(jet);
colorbar;
xlabel('x');
ylabel('y');
title('Sphere Function - Contour Plot');
grid on;
axis equal;
xlim([-10 10]);
ylim([-10 10]);

% Mark the global optimum
hold on;
plot(0, 0, 'y*', 'MarkerSize', 20, 'LineWidth', 2);
legend('', 'Global Optimum (0,0)', 'Location', 'northeast');
hold off;

% Create a third figure showing cross-sections
figure('Position', [100, 650, 1200, 400]);

% Subplot 1: Cross-section along x-axis (y=0)
subplot(1, 2, 1);
x_vals = linspace(-10, 10, 1000);
f_x = x_vals.^2;
plot(x_vals, f_x, 'b-', 'LineWidth', 2);
grid on;
xlabel('x (with y = 0)');
ylabel('f(x, 0)');
title('Cross-Section Along x-axis');
hold on;
plot(0, 0, 'ro', 'MarkerSize', 10, 'LineWidth', 2, 'MarkerFaceColor', 'r');
legend('f(x, 0) = x^2', 'Global minimum', 'Location', 'north');
hold off;

% Subplot 2: Radial cross-section
subplot(1, 2, 2);
r_vals = linspace(0, 10, 1000);
f_r = r_vals.^2;
plot(r_vals, f_r, 'b-', 'LineWidth', 2);
grid on;
xlabel('Distance from origin (r = sqrt(x^2 + y^2))', 'Interpreter', 'none');
ylabel('f(r)');
title('Radial Cross-Section');
hold on;
plot(0, 0, 'ro', 'MarkerSize', 10, 'LineWidth', 2, 'MarkerFaceColor', 'r');
legend('f(r) = r^2', 'Global minimum', 'Location', 'northwest');
hold off;

% Print some information
fprintf('Sphere Function Properties:\n');
fprintf('Function: f(x,y) = x^2 + y^2\n');
fprintf('For n dimensions: f(x) = sum(x_i^2) for i=1 to n\n');
fprintf('Domain: x, y in [-100, 100] (shown [-10, 10])\n');
fprintf('Global optimum: (0, 0) with f(0, 0) = 0\n');
fprintf('Properties:\n');
fprintf('  - Unimodal (single minimum)\n');
fprintf('  - Convex everywhere\n');
fprintf('  - Separable (each dimension independent)\n');
fprintf('  - Radially symmetric\n');
fprintf('  - Continuous and differentiable everywhere\n');
fprintf('  - Gradient: grad(f) = [2*x, 2*y]\n');
fprintf('  - One of the simplest test functions\n');
fprintf('  - Easy for most optimization algorithms\n');

