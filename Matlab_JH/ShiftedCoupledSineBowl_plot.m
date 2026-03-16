% Shifted Coupled Sine Bowl benchmark function visualization
% For 3 dimensions (d=3)

% Parameters
alpha = 0.4;
beta = 3.0;
gamma = 0.5;
delta = 0.15;
d = 3; % dimensions

% Shift vector: c_i = 0.4*cos(i)
c = zeros(1, d);
for i = 1:d
    c(i) = 0.4 * cos(i);
end

% Weights: w_i = 1 + 4*(i-1)/(d-1)
w = zeros(1, d);
for i = 1:d
    w(i) = 1.0 + 4.0 * (i-1) / (d-1);
end

% Display parameters
fprintf('Shifted Coupled Sine Bowl Function (d=%d)\n', d);
fprintf('Parameters: alpha=%.2f, beta=%.2f, gamma=%.2f, delta=%.2f\n', alpha, beta, gamma, delta);
fprintf('Shift vector c: [%.4f, %.4f, %.4f]\n', c(1), c(2), c(3));
fprintf('Weight vector w: [%.4f, %.4f, %.4f]\n', w(1), w(2), w(3));

% Define the fitness function
function fitness = shiftedCoupledSineBowl(x, c, w, alpha, beta, gamma, delta, d)
    fitness = 0.0;

    % First term: sum of weighted squared deviations
    for i = 1:d
        deviation = x(i) - c(i);
        fitness = fitness + w(i) * deviation^2;
    end

    % Second and third terms: coupling terms
    if d > 1
        for i = 1:(d-1)
            deviation_i = x(i) - c(i);
            deviation_i_plus_1 = x(i+1) - c(i+1);

            % Second term: sine coupling
            sinArg = beta * (deviation_i + gamma * deviation_i_plus_1);
            fitness = fitness + alpha * sin(sinArg)^2;

            % Third term: difference penalty
            diff = deviation_i - deviation_i_plus_1;
            fitness = fitness + delta * diff^2;
        end
    end
end

%% 1. 2D Slice Plots (fixing one dimension at shift value)
figure('Name', 'Shifted Coupled Sine Bowl - 2D Slices', 'Position', [100, 100, 1200, 800]);

% Create grid for 2D plots
n_points = 200;
x_range = linspace(-5, 5, n_points);
y_range = linspace(-5, 5, n_points);
[X, Y] = meshgrid(x_range, y_range);

% Subplot 1: x1-x2 plane (x3 fixed at c(3))
subplot(2, 3, 1);
Z = zeros(size(X));
for i = 1:numel(X)
    x_vec = [X(i), Y(i), c(3)];
    Z(i) = shiftedCoupledSineBowl(x_vec, c, w, alpha, beta, gamma, delta, d);
end
surf(X, Y, Z, 'EdgeColor', 'none');
xlabel('x_1'); ylabel('x_2'); zlabel('f(x)');
title(sprintf('x_1-x_2 plane (x_3=%.3f)', c(3)));
colorbar; view(45, 30);

% Subplot 2: Contour of x1-x2 plane
subplot(2, 3, 4);
contourf(X, Y, Z, 30);
xlabel('x_1'); ylabel('x_2');
title(sprintf('Contour: x_1-x_2 plane (x_3=%.3f)', c(3)));
colorbar;
hold on;
plot(c(1), c(2), 'r*', 'MarkerSize', 15, 'LineWidth', 2);
legend('Function values', 'Shift point');

% Subplot 3: x1-x3 plane (x2 fixed at c(2))
subplot(2, 3, 2);
Z = zeros(size(X));
for i = 1:numel(X)
    x_vec = [X(i), c(2), Y(i)];
    Z(i) = shiftedCoupledSineBowl(x_vec, c, w, alpha, beta, gamma, delta, d);
end
surf(X, Y, Z, 'EdgeColor', 'none');
xlabel('x_1'); ylabel('x_3'); zlabel('f(x)');
title(sprintf('x_1-x_3 plane (x_2=%.3f)', c(2)));
colorbar; view(45, 30);

% Subplot 4: Contour of x1-x3 plane
subplot(2, 3, 5);
contourf(X, Y, Z, 30);
xlabel('x_1'); ylabel('x_3');
title(sprintf('Contour: x_1-x_3 plane (x_2=%.3f)', c(2)));
colorbar;
hold on;
plot(c(1), c(3), 'r*', 'MarkerSize', 15, 'LineWidth', 2);
legend('Function values', 'Shift point');

% Subplot 5: x2-x3 plane (x1 fixed at c(1))
subplot(2, 3, 3);
Z = zeros(size(X));
for i = 1:numel(X)
    x_vec = [c(1), X(i), Y(i)];
    Z(i) = shiftedCoupledSineBowl(x_vec, c, w, alpha, beta, gamma, delta, d);
end
surf(X, Y, Z, 'EdgeColor', 'none');
xlabel('x_2'); ylabel('x_3'); zlabel('f(x)');
title(sprintf('x_2-x_3 plane (x_1=%.3f)', c(1)));
colorbar; view(45, 30);

% Subplot 6: Contour of x2-x3 plane
subplot(2, 3, 6);
contourf(X, Y, Z, 30);
xlabel('x_2'); ylabel('x_3');
title(sprintf('Contour: x_2-x_3 plane (x_1=%.3f)', c(1)));
colorbar;
hold on;
plot(c(2), c(3), 'r*', 'MarkerSize', 15, 'LineWidth', 2);
legend('Function values', 'Shift point');

%% 2. 1D Cross-sections through the optimum
figure('Name', 'Shifted Coupled Sine Bowl - 1D Cross-sections', 'Position', [150, 150, 1200, 400]);

x_1d = linspace(-5, 5, 500);

% Cross-section along x1 axis
subplot(1, 3, 1);
f_x1 = zeros(size(x_1d));
for i = 1:length(x_1d)
    x_vec = [x_1d(i), c(2), c(3)];
    f_x1(i) = shiftedCoupledSineBowl(x_vec, c, w, alpha, beta, gamma, delta, d);
end
plot(x_1d, f_x1, 'b-', 'LineWidth', 2);
xlabel('x_1'); ylabel('f(x)');
title(sprintf('Cross-section along x_1\n(x_2=%.3f, x_3=%.3f)', c(2), c(3)));
grid on;
hold on;
plot(c(1), shiftedCoupledSineBowl(c, c, w, alpha, beta, gamma, delta, d), 'r*', 'MarkerSize', 15, 'LineWidth', 2);
legend('Function', 'Optimum');

% Cross-section along x2 axis
subplot(1, 3, 2);
f_x2 = zeros(size(x_1d));
for i = 1:length(x_1d)
    x_vec = [c(1), x_1d(i), c(3)];
    f_x2(i) = shiftedCoupledSineBowl(x_vec, c, w, alpha, beta, gamma, delta, d);
end
plot(x_1d, f_x2, 'b-', 'LineWidth', 2);
xlabel('x_2'); ylabel('f(x)');
title(sprintf('Cross-section along x_2\n(x_1=%.3f, x_3=%.3f)', c(1), c(3)));
grid on;
hold on;
plot(c(2), shiftedCoupledSineBowl(c, c, w, alpha, beta, gamma, delta, d), 'r*', 'MarkerSize', 15, 'LineWidth', 2);
legend('Function', 'Optimum');

% Cross-section along x3 axis
subplot(1, 3, 3);
f_x3 = zeros(size(x_1d));
for i = 1:length(x_1d)
    x_vec = [c(1), c(2), x_1d(i)];
    f_x3(i) = shiftedCoupledSineBowl(x_vec, c, w, alpha, beta, gamma, delta, d);
end
plot(x_1d, f_x3, 'b-', 'LineWidth', 2);
xlabel('x_3'); ylabel('f(x)');
title(sprintf('Cross-section along x_3\n(x_1=%.3f, x_2=%.3f)', c(1), c(2)));
grid on;
hold on;
plot(c(3), shiftedCoupledSineBowl(c, c, w, alpha, beta, gamma, delta, d), 'r*', 'MarkerSize', 15, 'LineWidth', 2);
legend('Function', 'Optimum');

%% 3. 3D Visualization using isosurfaces
figure('Name', 'Shifted Coupled Sine Bowl - 3D Isosurfaces', 'Position', [200, 200, 800, 700]);

% Create 3D grid (coarser for computational efficiency)
n_3d = 50;
x1_3d = linspace(-5, 5, n_3d);
x2_3d = linspace(-5, 5, n_3d);
x3_3d = linspace(-5, 5, n_3d);
[X1, X2, X3] = meshgrid(x1_3d, x2_3d, x3_3d);

% Evaluate function on 3D grid
fprintf('Computing 3D grid (%d points)...\n', n_3d^3);
F = zeros(size(X1));
for i = 1:numel(X1)
    x_vec = [X1(i), X2(i), X3(i)];
    F(i) = shiftedCoupledSineBowl(x_vec, c, w, alpha, beta, gamma, delta, d);
end
fprintf('Done!\n');

% Plot multiple isosurfaces
levels = [0.5, 1, 2, 5, 10];
colors = jet(length(levels));
hold on;
for i = 1:length(levels)
    p = patch(isosurface(X1, X2, X3, F, levels(i)));
    isonormals(X1, X2, X3, F, p);
    set(p, 'FaceColor', colors(i,:), 'EdgeColor', 'none', 'FaceAlpha', 0.3);
end

% Mark the optimum (shift point)
plot3(c(1), c(2), c(3), 'r*', 'MarkerSize', 20, 'LineWidth', 3);
xlabel('x_1'); ylabel('x_2'); zlabel('x_3');
title('3D Isosurfaces of Shifted Coupled Sine Bowl');
legend([repmat('f=', length(levels), 1), num2str(levels')], 'Optimum', 'Location', 'best');
grid on;
view(45, 30);
camlight; lighting gouraud;
axis equal;
xlim([-5, 5]); ylim([-5, 5]); zlim([-5, 5]);

%% 4. Statistics
fprintf('\n=== Function Statistics ===\n');
fprintf('Global optimum location: c = [%.4f, %.4f, %.4f]\n', c(1), c(2), c(3));
fprintf('Global optimum value: f(c) = %.6f\n', shiftedCoupledSineBowl(c, c, w, alpha, beta, gamma, delta, d));

% Sample some random points
n_samples = 1000;
random_samples = rand(n_samples, d) * 10 - 5; % uniform in [-5, 5]
f_samples = zeros(n_samples, 1);
for i = 1:n_samples
    f_samples(i) = shiftedCoupledSineBowl(random_samples(i,:), c, w, alpha, beta, gamma, delta, d);
end

fprintf('Random sampling statistics (n=%d):\n', n_samples);
fprintf('  Mean: %.4f\n', mean(f_samples));
fprintf('  Std:  %.4f\n', std(f_samples));
fprintf('  Min:  %.4f\n', min(f_samples));
fprintf('  Max:  %.4f\n', max(f_samples));

fprintf('\nVisualization complete! Three figures created.\n');

