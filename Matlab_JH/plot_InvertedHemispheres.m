% MATLAB script to plot the Inverted Hemispheres function
% z(x,y) = base_height - max(0, sqrt(radius^2-(x-center_offset)^2-y^2),
%                                sqrt(radius^2-(x+center_offset)^2-y^2))

%% ADJUSTABLE PARAMETERS - Change these values to modify the function
base_height = 2;      % Base height of the surface (the '2' in the original formula)
center_offset = 2;  % Offset of hemisphere centers from origin (the '1.3' in the original formula)
radius = 1.7;           % Radius of the hemispheres

% Define the domain
x = linspace(-4, 4, 500);
y = linspace(-4, 4, 500);
[X, Y] = meshgrid(x, y);

% Initialize Z matrix
Z = zeros(size(X));

% Calculate the function value for each point
for i = 1:length(y)
    for j = 1:length(x)
        xval = X(i,j);
        yval = Y(i,j);

        % Calculate the hemisphere values
        % Right hemisphere centered at (1.3, 0)
        dist_sq_right = (xval - center_offset)^2 + yval^2;
        if dist_sq_right <= radius^2
            hem_right = sqrt(radius^2 - dist_sq_right);
        else
            hem_right = 0;
        end

        % Left hemisphere centered at (-1.3, 0)
        dist_sq_left = (xval + center_offset)^2 + yval^2;
        if dist_sq_left <= radius^2
            hem_left = sqrt(radius^2 - dist_sq_left);
        else
            hem_left = 0;
        end

        % Calculate z = 2 - max(0, hem_right, hem_left)
        Z(i,j) = base_height - max([0, hem_right, hem_left]);
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
zlabel('z(x,y)');
title('Inverted Hemispheres Function - 3D View');
view(-30, 30);
grid on;
axis tight;

% Mark the hemisphere centers and minima
hold on;
% Left hemisphere center at (-1.3, 0)
plot3(-center_offset, 0, base_height, 'ko', 'MarkerSize', 8, 'LineWidth', 2);
% Right hemisphere center at (1.3, 0)
plot3(center_offset, 0, base_height, 'ko', 'MarkerSize', 8, 'LineWidth', 2);
% Global minima at bottoms of hemispheres
plot3(-center_offset, 0, base_height - radius, 'y*', 'MarkerSize', 20, 'LineWidth', 2);
plot3(center_offset, 0, base_height - radius, 'y*', 'MarkerSize', 20, 'LineWidth', 2);
hold off;

% Subplot 2: Contour plot
subplot(1, 2, 2);
contourf(X, Y, Z, 30);
colormap(jet);
colorbar;
xlabel('x');
ylabel('y');
title('Inverted Hemispheres Function - Contour Plot');
grid on;
axis equal;
xlim([-4 4]);
ylim([-4 4]);

% Mark important features
hold on;
% Mark the hemisphere centers
plot(-center_offset, 0, 'ko', 'MarkerSize', 8, 'LineWidth', 2, 'MarkerFaceColor', 'k');
plot(center_offset, 0, 'ko', 'MarkerSize', 8, 'LineWidth', 2, 'MarkerFaceColor', 'k');
% Mark the global minima
plot(-center_offset, 0, 'y*', 'MarkerSize', 20, 'LineWidth', 2);
plot(center_offset, 0, 'y*', 'MarkerSize', 20, 'LineWidth', 2);
% Draw circles showing hemisphere boundaries
theta = linspace(0, 2*pi, 100);
plot(-center_offset + radius*cos(theta), radius*sin(theta), 'r--', 'LineWidth', 1.5);
plot(center_offset + radius*cos(theta), radius*sin(theta), 'r--', 'LineWidth', 1.5);
hold off;

% Print some information
fprintf('Inverted Hemispheres Function Properties:\n');
fprintf('Function: z(x,y) = %.1f - max(0, sqrt(%.1f-(x-%.1f)^2-y^2), sqrt(%.1f-(x+%.1f)^2-y^2))\n', ...
        base_height, radius^2, center_offset, radius^2, center_offset);
fprintf('Base height: %.1f\n', base_height);
fprintf('Hemisphere radius: %.1f\n', radius);
fprintf('Left hemisphere center: (%.1f, 0)\n', -center_offset);
fprintf('Right hemisphere center: (%.1f, 0)\n', center_offset);
fprintf('Global minima: z(%.1f, 0) = %.1f and z(%.1f, 0) = %.1f\n', ...
        -center_offset, base_height - radius, center_offset, base_height - radius);
fprintf('Maximum value (outside hemispheres): %.1f\n', base_height);

