% MATLAB script to plot the Inverted Tanh Radial Step function
% f(x,y) = -h * (1/2) * (1 - tanh(k * (sqrt(x^2 + y^2) - r)))
%        = h * (1/2) * (tanh(k * (sqrt(x^2 + y^2) - r)) - 1)
%
% This creates a smooth radial depression/valley that transitions to zero
% using a hyperbolic tangent function

%% ADJUSTABLE PARAMETERS - Change these values to modify the function
h = 10;        % Height of the central plateau
k = 2;         % Steepness of transition (larger k = sharper edge)
r = 3;         % Radius where transition occurs

% Define the domain
x = linspace(-10, 10, 500);
y = linspace(-10, 10, 500);
[X, Y] = meshgrid(x, y);

% Calculate the function value for each point
% Distance from origin
R = sqrt(X.^2 + Y.^2);

% Apply the inverted tanh radial step function (creates a valley/depression)
Z = -h * 0.5 * (1 - tanh(k * (R - r)));

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
title('Inverted Tanh Radial Step Function - 3D View');
view(-30, 30);
grid on;
axis tight;

% Mark the center (bottom of valley)
hold on;
plot3(0, 0, -h*0.5, 'ko', 'MarkerSize', 10, 'LineWidth', 2, 'MarkerFaceColor', 'k');
% Mark the transition radius circle at z = -h/2
theta = linspace(0, 2*pi, 100);
plot3(r*cos(theta), r*sin(theta), -h*0.5*ones(size(theta)), 'r--', 'LineWidth', 2);
hold off;

% Subplot 2: Contour plot
subplot(1, 2, 2);
contourf(X, Y, Z, 30);
colormap(jet);
colorbar;
xlabel('x');
ylabel('y');
title('Inverted Tanh Radial Step Function - Contour Plot');
grid on;
axis equal;
xlim([-10 10]);
ylim([-10 10]);

% Mark important features
hold on;
% Mark the center
plot(0, 0, 'ko', 'MarkerSize', 10, 'LineWidth', 2, 'MarkerFaceColor', 'k');
% Draw circle showing transition radius
plot(r*cos(theta), r*sin(theta), 'r--', 'LineWidth', 2);
legend('', 'Center', 'Transition Radius', 'Location', 'northeast');
hold off;

% Create a third figure showing the radial cross-section
figure('Position', [100, 650, 800, 400]);
r_vals = linspace(0, 10, 1000);
f_vals = -h * 0.5 * (1 - tanh(k * (r_vals - r)));
plot(r_vals, f_vals, 'b-', 'LineWidth', 2);
grid on;
xlabel('Distance from center (r = \sqrt{x^2 + y^2})');
ylabel('f(r)');
title('Radial Cross-Section (Inverted)');
hold on;
% Mark the transition point
plot(r, -h*0.5, 'ro', 'MarkerSize', 10, 'LineWidth', 2, 'MarkerFaceColor', 'r');
% Mark valley depth
yline(-h, 'g--', 'LineWidth', 1.5, 'Label', sprintf('Valley depth = %.1f', -h));
% Mark transition radius
xline(r, 'r--', 'LineWidth', 1.5, 'Label', sprintf('Transition radius = %.1f', r));
legend('f(r)', 'Transition point (r, -h/2)', 'Location', 'southeast');
hold off;

% Print some information
fprintf('Inverted Tanh Radial Step Function Properties:\n');
fprintf('Function: f(x,y) = -%.1f * (1/2) * (1 - tanh(%.1f * (sqrt(x^2 + y^2) - %.1f)))\n', h, k, r);
fprintf('Height parameter: h = %.1f\n', h);
fprintf('Steepness parameter: k = %.1f (larger = sharper transition)\n', k);
fprintf('Transition radius: r = %.1f\n', r);
fprintf('Value at center (0,0): f(0,0) = %.6f\n', -h * 0.5 * (1 - tanh(k * (0 - r))));
fprintf('Value at transition radius: f(%.1f,0) = %.6f (should be -h/2 = %.1f)\n', r, -h * 0.5 * (1 - tanh(k * (r - r))), -h*0.5);
fprintf('Function approaches 0 as distance → ∞\n');
fprintf('\nPhysical interpretation:\n');
fprintf('- Creates a smooth valley or depression shape\n');
fprintf('- Center is lowest (approaching -h when r < transition)\n');
fprintf('- Smooth transition around radius r\n');
fprintf('- Approaches 0 far from center (flat ground)\n');
fprintf('- tanh creates smooth, S-shaped transition (no discontinuities)\n');
fprintf('- Perfect for optimization: single global minimum at center!\n');

