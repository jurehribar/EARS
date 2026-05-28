% MATLAB script to plot the Piecewise Linear 1D function (y-dependent)
% F(x,y) = 10-y,   0 <= y < 4
%        = 6,       4 <= y <= 5  (PLATEAU)
%        = 11-y,    5 < y <= 10
%
% Note: F depends only on y, so the surface is constant along x-axis (ridges)

% Define the domain
x = linspace(0, 10, 600);
y = linspace(0, 10, 600);
[X, Y] = meshgrid(x, y);

% Initialize Z matrix
Z = zeros(size(X));

% Calculate the function value for each point
for i = 1:length(y)
    for j = 1:length(x)
        yval = Y(i,j);

        if yval < 4
            Z(i,j) = 10 - yval;
        elseif yval <= 5
            Z(i,j) = 6;
        else
            Z(i,j) = 11 - yval;
        end
    end
end

% Create the plots
figure('Position', [100, 100, 1200, 500]);

% Subplot 1: 3D surface
subplot(1, 2, 1);
surf(X, Y, Z, 'EdgeColor', 'none');
colormap(jet);
colorbar;
xlabel('x');
ylabel('y');
zlabel('F(x,y)');
title('Piecewise Linear 1D (y) Function - 3D View');
view(-30, 30);
grid on;
axis tight;

% Mark the plateau boundaries
hold on;
plot3([0 10], [4 4], [6 6], 'r-', 'LineWidth', 2);
plot3([0 10], [5 5], [6 6], 'r-', 'LineWidth', 2);
hold off;

% Subplot 2: Contour plot
subplot(1, 2, 2);
contourf(X, Y, Z, 30);
colormap(jet);
colorbar;
xlabel('x');
ylabel('y');
title('Piecewise Linear 1D (y) Function - Contour Plot');
grid on;
xlim([0 10]);
ylim([0 10]);

% Mark the plateau boundaries
hold on;
yline(4, 'r--', 'LineWidth', 2, 'Label', 'y = 4');
yline(5, 'r--', 'LineWidth', 2, 'Label', 'y = 5');
hold off;

% Figure 2: 1D cross-section along y
figure('Position', [100, 650, 700, 400]);

y_line = linspace(0, 10, 1000);
f_line = zeros(size(y_line));
for i = 1:length(y_line)
    yval = y_line(i);
    if yval < 4
        f_line(i) = 10 - yval;
    elseif yval <= 5
        f_line(i) = 6;
    else
        f_line(i) = 11 - yval;
    end
end

plot(y_line, f_line, 'b-', 'LineWidth', 2);
hold on;
fill([4 5 5 4], [5.8 5.8 6.2 6.2], 'r', 'FaceAlpha', 0.2, 'EdgeColor', 'none');
xline(4, 'r--', 'LineWidth', 1.5);
xline(5, 'r--', 'LineWidth', 1.5);
plot(0,  10, 'ko', 'MarkerFaceColor', 'k', 'MarkerSize', 6);
plot(4,   6, 'ro', 'MarkerFaceColor', 'r', 'MarkerSize', 8);
plot(5,   6, 'ro', 'MarkerFaceColor', 'r', 'MarkerSize', 8);
plot(10,  1, 'ko', 'MarkerFaceColor', 'k', 'MarkerSize', 6);
hold off;
grid on;
xlabel('y');
ylabel('F(y)');
title('Cross-section F(y) - function depends only on y');
legend('F(y)', 'Plateau region [4,5]', '', '', '', '', '', 'Location', 'best');
ylim([0 11]);
xlim([0 10]);

% Print some information
fprintf('Piecewise Linear 1D (y) Function Properties:\n');
fprintf('F(x,y) depends only on y!\n');
fprintf('Region 1: F = 10 - y  for  0 <= y < 4  (decreasing from 10 to 6)\n');
fprintf('Region 2: F = 6        for  4 <= y <= 5 (flat PLATEAU)\n');
fprintf('Region 3: F = 11 - y  for  5 <  y <= 10 (decreasing from 6 to 1)\n');
fprintf('Value at y=0:  F = 10\n');
fprintf('Value at y=4:  F = 6  (left edge of plateau)\n');
fprintf('Value at y=5:  F = 6  (right edge of plateau)\n');
fprintf('Value at y=10: F = 1  (minimum)\n');

