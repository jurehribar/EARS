% MATLAB script to plot the 2D Rastrigin function
% f(x,y) = 20 + x^2 - 10*cos(2*pi*x) + y^2 - 10*cos(2*pi*y)
% Domain: x, y in [-5.12, 5.12]

%% ADJUSTABLE PARAMETERS
A = 10;  % Amplitude parameter (standard is 10)

% Define the domain
x = linspace(-5.12, 5.12, 500);
y = linspace(-5.12, 5.12, 500);
[X, Y] = meshgrid(x, y);

% Calculate the Rastrigin function
n = 2;  % Number of dimensions
Z = A * n + (X.^2 - A * cos(2*pi*X)) + (Y.^2 - A * cos(2*pi*Y));

% Create the plots
figure('Position', [100, 100, 1200, 500]);

% Subplot 1: 3D surface
subplot(1, 2, 1);
surf(X, Y, Z, 'EdgeColor', 'none');
colormap(jet);
colorbar;
xlabel('x');
ylabel('y');
zlabel('f(x,y)');
title('Rastrigin Function - 3D View');
view(-30, 30);
grid on;
axis tight;

% Mark the global optimum at (0, 0)
hold on;
plot3(0, 0, 0, 'y*', 'MarkerSize', 20, 'LineWidth', 2);
hold off;

% Subplot 2: Contour plot
subplot(1, 2, 2);
contourf(X, Y, Z, 50);
colormap(jet);
colorbar;
xlabel('x');
ylabel('y');
title('Rastrigin Function - Contour Plot');
grid on;
axis equal;
xlim([-5.12 5.12]);
ylim([-5.12 5.12]);

% Mark the global optimum
hold on;
plot(0, 0, 'y*', 'MarkerSize', 20, 'LineWidth', 2);
hold off;

% Create cross-section plots
figure('Position', [100, 100, 1200, 400]);

% Cross-section along x-axis (y=0)
subplot(1, 2, 1);
x_line = linspace(-5.12, 5.12, 1000);
z_x = A + x_line.^2 - A * cos(2*pi*x_line);
plot(x_line, z_x, 'b-', 'LineWidth', 2);
hold on;
plot(0, 0, 'y*', 'MarkerSize', 15, 'LineWidth', 2);
hold off;
grid on;
xlabel('x');
ylabel('f(x, 0)');
title('Cross-section along x-axis (y=0)');

% Cross-section along y-axis (x=0)
subplot(1, 2, 2);
y_line = linspace(-5.12, 5.12, 1000);
z_y = A + y_line.^2 - A * cos(2*pi*y_line);
plot(y_line, z_y, 'r-', 'LineWidth', 2);
hold on;
plot(0, 0, 'y*', 'MarkerSize', 15, 'LineWidth', 2);
hold off;
grid on;
xlabel('y');
ylabel('f(0, y)');
title('Cross-section along y-axis (x=0)');

% Print some information
fprintf('Rastrigin Function Properties:\n');
fprintf('Function: f(x,y) = %d*%d + (x^2 - %d*cos(2*pi*x)) + (y^2 - %d*cos(2*pi*y))\n', ...
        A, n, A, A);
fprintf('Domain: x, y in [-5.12, 5.12]\n');
fprintf('Global minimum: f(0, 0) = %.1f\n', 0);
fprintf('Approximate number of local minima: ~25-30\n');
fprintf('Properties: Highly multimodal, separable, smooth\n');

