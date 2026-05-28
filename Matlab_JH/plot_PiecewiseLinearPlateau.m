% MATLAB script to plot the Piecewise Linear Plateau function
% F(x,y) = x+y          if x<3, y<3
%        = 3+y          if 3≤x≤5, y<3
%        = x+3          if x<3, 3≤y≤5
%        = 6            if 3≤x≤5, 3≤y≤5 (PLATEAU)
%        = (x-2)+y      if x>5, y<3
%        = x+(y-2)      if x<3, y>5
%        = (x-2)+(y-2)  if x>5, y>5

% Define the domain
x = linspace(-2, 10, 600);
y = linspace(-2, 10, 600);
[X, Y] = meshgrid(x, y);

% Initialize Z matrix
Z = zeros(size(X));

% Calculate the function value for each point
for i = 1:length(y)
    for j = 1:length(x)
        xval = X(i,j);
        yval = Y(i,j);

        % Apply piecewise function
        if xval < 3 && yval < 3
            % Region 1: x+y
            Z(i,j) = xval + yval;
        elseif xval >= 3 && xval <= 5 && yval < 3
            % Region 2: 3+y
            Z(i,j) = 3 + yval;
        elseif xval < 3 && yval >= 3 && yval <= 5
            % Region 3: x+3
            Z(i,j) = xval + 3;
        elseif xval >= 3 && xval <= 5 && yval >= 3 && yval <= 5
            % Region 4: PLATEAU at 6
            Z(i,j) = 6;
        elseif xval > 5 && yval < 3
            % Region 5: (x-2)+y
            Z(i,j) = (xval - 2) + yval;
        elseif xval < 3 && yval > 5
            % Region 6: x+(y-2)
            Z(i,j) = xval + (yval - 2);
        elseif xval > 5 && yval > 5
            % Region 7: (x-2)+(y-2)
            Z(i,j) = (xval - 2) + (yval - 2);
        else
            % Edge cases at x=5 or y=5
            if xval > 5 && yval >= 3 && yval <= 5
                Z(i,j) = (xval - 2) + yval;
            elseif xval >= 3 && xval <= 5 && yval > 5
                Z(i,j) = xval + (yval - 2);
            else
                Z(i,j) = xval + yval;
            end
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
title('Piecewise Linear Plateau Function - 3D View');
view(-30, 30);
grid on;
axis tight;

% Mark the plateau boundaries
hold on;
% Draw red rectangle outline at plateau region
plateau_x = [3, 5, 5, 3, 3];
plateau_y = [3, 3, 5, 5, 3];
plateau_z = [6, 6, 6, 6, 6];
plot3(plateau_x, plateau_y, plateau_z, 'r-', 'LineWidth', 2);
hold off;

% Subplot 2: Contour plot
subplot(1, 2, 2);
contourf(X, Y, Z, 30);
colormap(jet);
colorbar;
xlabel('x');
ylabel('y');
title('Piecewise Linear Plateau Function - Contour Plot');
grid on;
axis equal;
xlim([-2 10]);
ylim([-2 10]);

% Mark the plateau boundaries
hold on;
% Draw boundary lines
xline(3, 'r--', 'LineWidth', 1.5);
xline(5, 'r--', 'LineWidth', 1.5);
yline(3, 'r--', 'LineWidth', 1.5);
yline(5, 'r--', 'LineWidth', 1.5);
% Draw plateau rectangle
plot([3, 5, 5, 3, 3], [3, 3, 5, 5, 3], 'r-', 'LineWidth', 2);
hold off;

% Figure 2: Cross-sections
figure('Position', [100, 650, 1200, 400]);

% Cross-section along x-axis (y=1)
subplot(1, 2, 1);
y_fixed = 1;
x_line = linspace(-2, 10, 600);
f_line = zeros(size(x_line));

for i = 1:length(x_line)
    xval = x_line(i);
    yval = y_fixed;

    if xval < 3 && yval < 3
        f_line(i) = xval + yval;
    elseif xval >= 3 && xval <= 5 && yval < 3
        f_line(i) = 3 + yval;
    elseif xval > 5 && yval < 3
        f_line(i) = (xval - 2) + yval;
    else
        f_line(i) = xval + yval;
    end
end

plot(x_line, f_line, 'b-', 'LineWidth', 2);
grid on;
xlabel('x');
ylabel(['F(x, ' num2str(y_fixed) ')']);
title(['Cross-section along x-axis (y = ' num2str(y_fixed) ')']);
xline(3, 'r--', 'LineWidth', 1);
xline(5, 'r--', 'LineWidth', 1);

% Cross-section along y-axis (x=1)
subplot(1, 2, 2);
x_fixed = 1;
y_line = linspace(-2, 10, 600);
f_line = zeros(size(y_line));

for i = 1:length(y_line)
    xval = x_fixed;
    yval = y_line(i);

    if xval < 3 && yval < 3
        f_line(i) = xval + yval;
    elseif xval < 3 && yval >= 3 && yval <= 5
        f_line(i) = xval + 3;
    elseif xval < 3 && yval > 5
        f_line(i) = xval + (yval - 2);
    else
        f_line(i) = xval + yval;
    end
end

plot(y_line, f_line, 'b-', 'LineWidth', 2);
grid on;
xlabel('y');
ylabel(['F(' num2str(x_fixed) ', y)']);
title(['Cross-section along y-axis (x = ' num2str(x_fixed) ')']);
yline(3, 'r--', 'LineWidth', 1);
yline(5, 'r--', 'LineWidth', 1);

% Print some information
fprintf('Piecewise Linear Plateau Function Properties:\n');
fprintf('Plateau region: x ∈ [3, 5], y ∈ [3, 5]\n');
fprintf('Plateau value: F = 6\n');
fprintf('Function has 7 distinct regions\n');
fprintf('All regions are piecewise linear\n');

