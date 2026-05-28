% MATLAB script to plot the Piecewise Linear 1D function (extended to 2D)
% F(x,y) = 10-x,   0 <= x < 4
%        = 6,       4 <= x <= 5  (PLATEAU)
%        = 11-x,    5 < x <= 10
%
% Note: F depends only on x, so the surface is constant along y-axis (ridges)

% Define the domain
x = linspace(0, 10, 600);
y = linspace(0, 10, 600);
[X, Y] = meshgrid(x, y);

% Initialize Z matrix
Z = zeros(size(X));

% Calculate the function value for each point
for i = 1:length(y)
    for j = 1:length(x)
        xval = X(i,j);

        if xval < 4
            % Region 1: 10 - x
            Z(i,j) = 10 - xval;
        elseif xval <= 5
            % Region 2: PLATEAU at 6
            Z(i,j) = 6;
        else
            % Region 3: 11 - x
            Z(i,j) = 11 - xval;
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
title('Piecewise Linear 1D Function - 3D View');
view(-30, 30);
grid on;
axis tight;

% Mark the plateau region
hold on;
% Draw red lines at plateau boundaries (x=4 and x=5) on top of surface
plot3([4 4], [0 10], [6 6], 'r-', 'LineWidth', 2);
plot3([5 5], [0 10], [6 6], 'r-', 'LineWidth', 2);
hold off;

% Subplot 2: Contour plot
subplot(1, 2, 2);
contourf(X, Y, Z, 30);
colormap(jet);
colorbar;
xlabel('x');
ylabel('y');
title('Piecewise Linear 1D Function - Contour Plot');
grid on;
xlim([0 10]);
ylim([0 10]);

% Mark the plateau boundaries
hold on;
xline(4, 'r--', 'LineWidth', 2, 'Label', 'x = 4');
xline(5, 'r--', 'LineWidth', 2, 'Label', 'x = 5');
hold off;

% Figure 2: 1D cross-section (the essential view since F depends only on x)
figure('Position', [100, 650, 700, 400]);

x_line = linspace(0, 10, 1000);
f_line = zeros(size(x_line));
for i = 1:length(x_line)
    xval = x_line(i);
    if xval < 4
        f_line(i) = 10 - xval;
    elseif xval <= 5
        f_line(i) = 6;
    else
        f_line(i) = 11 - xval;
    end
end

plot(x_line, f_line, 'b-', 'LineWidth', 2);
hold on;
% Highlight plateau region
fill([4 5 5 4], [5.8 5.8 6.2 6.2], 'r', 'FaceAlpha', 0.2, 'EdgeColor', 'none');
xline(4, 'r--', 'LineWidth', 1.5);
xline(5, 'r--', 'LineWidth', 1.5);
% Mark key points
plot(0,  10, 'ko', 'MarkerFaceColor', 'k', 'MarkerSize', 6);
plot(4,   6, 'ro', 'MarkerFaceColor', 'r', 'MarkerSize', 8);
plot(5,   6, 'ro', 'MarkerFaceColor', 'r', 'MarkerSize', 8);
plot(10,  1, 'ko', 'MarkerFaceColor', 'k', 'MarkerSize', 6);
hold off;
grid on;
xlabel('x');
ylabel('F(x)');
title('Cross-section F(x) - function depends only on x');
legend('F(x)', 'Plateau region [4,5]', '', '', '', '', '', 'Location', 'best');
ylim([0 11]);
xlim([0 10]);

% Print some information
fprintf('Piecewise Linear 1D Function Properties:\n');
fprintf('F(x,y) depends only on x!\n');
fprintf('Region 1: F = 10 - x  for  0 <= x < 4  (decreasing from 10 to 6)\n');
fprintf('Region 2: F = 6        for  4 <= x <= 5 (flat PLATEAU)\n');
fprintf('Region 3: F = 11 - x  for  5 <  x <= 10 (decreasing from 6 to 1)\n');
fprintf('Plateau value: F = 6\n');
fprintf('Value at x=0:  F = 10\n');
fprintf('Value at x=4:  F = 6  (left edge of plateau)\n');
fprintf('Value at x=5:  F = 6  (right edge of plateau)\n');
fprintf('Value at x=10: F = 1\n');
fprintf('Note: Function is continuous everywhere (no jumps at boundaries)\n');

