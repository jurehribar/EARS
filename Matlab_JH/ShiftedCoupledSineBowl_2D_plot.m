function ShiftedCoupledSineBowl_2D_plot(solution_point)
% Shifted Coupled Sine Bowl benchmark function visualization
% For 2 dimensions (d=2)
%
% Usage:
%   ShiftedCoupledSineBowl_2D_plot()  % Plot without solution point
%   ShiftedCoupledSineBowl_2D_plot([0.21607417265981194, -0.16618333991121975])  % Plot with solution point
%
% Input:
%   solution_point (optional) - 2D point to plot in yellow [x1, x2]

    % Check if solution_point was provided
    if nargin < 1
        solution_point = [];
    end

    % Validate solution_point if provided
    if ~isempty(solution_point)
        if length(solution_point) ~= 2
            error('solution_point must be a 2D vector [x1, x2]');
        end
        fprintf('Solution point provided: [%.15f, %.15f]\n', solution_point(1), solution_point(2));
    end

    % Parameters
    alpha = 10;%0.4;
    beta = 3.0;
    gamma = 0.5;
    delta = 0.15;
    d = 2; % dimensions

    % Shift vector: c_i = 0.4*cos(i)
    c = zeros(1, d);
    for i = 1:d
        c(i) = 0.4 * cos(i);
    end

    % Weights: w_i = 1 + 4*(i-1)/(d-1)
    w = zeros(1, d);
    for i = 1:d
        w(i) = 1.0 + 25.0 * (i-1) / (d-1);
    end

    % Display parameters
    fprintf('=== Shifted Coupled Sine Bowl Function (d=%d) ===\n', d);
    fprintf('Parameters: alpha=%.2f, beta=%.2f, gamma=%.2f, delta=%.2f\n', alpha, beta, gamma, delta);
    fprintf('Shift vector c: [%.6f, %.6f]\n', c(1), c(2));
    fprintf('Weight vector w: [%.6f, %.6f]\n', w(1), w(2));


    %% 1. Main 3D Surface Plot
    figure('Name', 'Shifted Coupled Sine Bowl 2D - Surface', 'Position', [100, 100, 1400, 900]);

    % Create high-resolution grid
    n_points = 300;
    x1_range = linspace(-5, 5, n_points);
    x2_range = linspace(-5, 5, n_points);
    [X1, X2] = meshgrid(x1_range, x2_range);

    % Evaluate function
    fprintf('Computing function values on %dx%d grid...\n', n_points, n_points);
    Z = zeros(size(X1));
    for i = 1:numel(X1)
        x_vec = [X1(i), X2(i)];
        Z(i) = shiftedCoupledSineBowl(x_vec, c, w, alpha, beta, gamma, delta, d);
    end
    fprintf('Done!\n');

    % Subplot 1: 3D Surface with colormap
    subplot(2, 3, 1);
    surf(X1, X2, Z, 'EdgeColor', 'none');
    xlabel('x_1'); ylabel('x_2'); zlabel('f(x_1, x_2)');
    title('3D Surface Plot');
    colorbar;
    view(45, 30);
    hold on;
    plot3(c(1), c(2), shiftedCoupledSineBowl(c, c, w, alpha, beta, gamma, delta, d), ...
        'r*', 'MarkerSize', 20, 'LineWidth', 3);
    % Plot solution point if provided
    if ~isempty(solution_point)
        f_sol = shiftedCoupledSineBowl(solution_point, c, w, alpha, beta, gamma, delta, d);
        plot3(solution_point(1), solution_point(2), f_sol, ...
            'y*', 'MarkerSize', 20, 'LineWidth', 3);
    end
    camlight; lighting gouraud;

    % Subplot 2: Top view
    subplot(2, 3, 2);
    surf(X1, X2, Z, 'EdgeColor', 'none');
    xlabel('x_1'); ylabel('x_2'); zlabel('f(x_1, x_2)');
    title('Top View');
    colorbar;
    view(0, 90);
    hold on;
    plot3(c(1), c(2), shiftedCoupledSineBowl(c, c, w, alpha, beta, gamma, delta, d), ...
        'r*', 'MarkerSize', 20, 'LineWidth', 3);
    % Plot solution point if provided
    if ~isempty(solution_point)
        f_sol = shiftedCoupledSineBowl(solution_point, c, w, alpha, beta, gamma, delta, d);
        plot3(solution_point(1), solution_point(2), f_sol, ...
            'y*', 'MarkerSize', 20, 'LineWidth', 3);
    end

    % Subplot 3: Side view
    subplot(2, 3, 3);
    surf(X1, X2, Z, 'EdgeColor', 'none');
    xlabel('x_1'); ylabel('x_2'); zlabel('f(x_1, x_2)');
    title('Side View');
    colorbar;
    view(0, 0);
    hold on;
    plot3(c(1), c(2), shiftedCoupledSineBowl(c, c, w, alpha, beta, gamma, delta, d), ...
        'r*', 'MarkerSize', 20, 'LineWidth', 3);
    % Plot solution point if provided
    if ~isempty(solution_point)
        f_sol = shiftedCoupledSineBowl(solution_point, c, w, alpha, beta, gamma, delta, d);
        plot3(solution_point(1), solution_point(2), f_sol, ...
            'y*', 'MarkerSize', 20, 'LineWidth', 3);
    end

    % Subplot 4: Filled contour plot
    subplot(2, 3, 4);
    contourf(X1, X2, Z, 40);
    xlabel('x_1'); ylabel('x_2');
    title('Filled Contour Plot');
    colorbar;
    hold on;
    plot(c(1), c(2), 'r*', 'MarkerSize', 20, 'LineWidth', 3);
    text(c(1)+0.3, c(2)+0.3, sprintf('Optimum\n(%.3f, %.3f)', c(1), c(2)), ...
        'Color', 'red', 'FontSize', 10, 'FontWeight', 'bold');
    % Plot solution point if provided
    if ~isempty(solution_point)
        plot(solution_point(1), solution_point(2), 'y*', 'MarkerSize', 20, 'LineWidth', 3);
        text(solution_point(1)+0.3, solution_point(2)-0.3, sprintf('Solution\n(%.3f, %.3f)', solution_point(1), solution_point(2)), ...
            'Color', 'yellow', 'FontSize', 10, 'FontWeight', 'bold', 'BackgroundColor', 'black');
    end
    grid on;

    % Subplot 5: Contour lines
    subplot(2, 3, 5);
    contour(X1, X2, Z, 30, 'LineWidth', 1.5);
    xlabel('x_1'); ylabel('x_2');
    title('Contour Lines');
    colorbar;
    hold on;
    plot(c(1), c(2), 'r*', 'MarkerSize', 20, 'LineWidth', 3);
    % Plot solution point if provided
    if ~isempty(solution_point)
        plot(solution_point(1), solution_point(2), 'y*', 'MarkerSize', 20, 'LineWidth', 3);
    end
    grid on;

    % Subplot 6: Log-scale contour (better for viewing near optimum)
    subplot(2, 3, 6);
    Z_log = log10(Z + 1e-10); % Add small value to avoid log(0)
    contourf(X1, X2, Z_log, 40);
    xlabel('x_1'); ylabel('x_2');
    title('Log-scale Contour Plot');
    colorbar;
    hold on;
    plot(c(1), c(2), 'r*', 'MarkerSize', 20, 'LineWidth', 3);
    % Plot solution point if provided
    if ~isempty(solution_point)
        plot(solution_point(1), solution_point(2), 'y*', 'MarkerSize', 20, 'LineWidth', 3);
    end
    grid on;

    %% 2. Cross-sections through optimum
    figure('Name', 'Shifted Coupled Sine Bowl 2D - Cross-sections', 'Position', [150, 150, 1400, 500]);

    x_1d = linspace(-5, 5, 500);

    % Subplot 1: Cross-section along x1 (x2 = c(2))
    subplot(1, 3, 1);
    f_x1 = zeros(size(x_1d));
    for i = 1:length(x_1d)
        x_vec = [x_1d(i), c(2)];
        f_x1(i) = shiftedCoupledSineBowl(x_vec, c, w, alpha, beta, gamma, delta, d);
    end
    plot(x_1d, f_x1, 'b-', 'LineWidth', 2);
    xlabel('x_1'); ylabel('f(x)');
    title(sprintf('Cross-section along x_1 axis\n(x_2 = %.4f)', c(2)));
    grid on;
    hold on;
    plot(c(1), shiftedCoupledSineBowl(c, c, w, alpha, beta, gamma, delta, d), ...
        'r*', 'MarkerSize', 15, 'LineWidth', 2);
    legend('f(x_1, c_2)', 'Optimum', 'Location', 'best');

    % Subplot 2: Cross-section along x2 (x1 = c(1))
    subplot(1, 3, 2);
    f_x2 = zeros(size(x_1d));
    for i = 1:length(x_1d)
        x_vec = [c(1), x_1d(i)];
        f_x2(i) = shiftedCoupledSineBowl(x_vec, c, w, alpha, beta, gamma, delta, d);
    end
    plot(x_1d, f_x2, 'b-', 'LineWidth', 2);
    xlabel('x_2'); ylabel('f(x)');
    title(sprintf('Cross-section along x_2 axis\n(x_1 = %.4f)', c(1)));
    grid on;
    hold on;
    plot(c(2), shiftedCoupledSineBowl(c, c, w, alpha, beta, gamma, delta, d), ...
        'r*', 'MarkerSize', 15, 'LineWidth', 2);
    legend('f(c_1, x_2)', 'Optimum', 'Location', 'best');

    % Subplot 3: Cross-section along diagonal
    subplot(1, 3, 3);
    f_diag = zeros(size(x_1d));
    for i = 1:length(x_1d)
        % Diagonal from (-5,-5) to (5,5) through the optimum
        t = (x_1d(i) + 5) / 10; % parameter from 0 to 1
        x1_diag = -5 + t * 10;
        x2_diag = -5 + t * 10;
        x_vec = [x1_diag, x2_diag];
        f_diag(i) = shiftedCoupledSineBowl(x_vec, c, w, alpha, beta, gamma, delta, d);
    end
    plot(x_1d, f_diag, 'b-', 'LineWidth', 2);
    xlabel('t (diagonal parameter)'); ylabel('f(x)');
    title('Cross-section along diagonal x_1 = x_2');
    grid on;
    hold on;
    % Find where diagonal crosses optimum
    t_opt = (c(1) + 5) / 10 * 10 - 5;
    plot(t_opt, shiftedCoupledSineBowl(c, c, w, alpha, beta, gamma, delta, d), ...
        'r*', 'MarkerSize', 15, 'LineWidth', 2);
    legend('f(t, t)', 'Optimum', 'Location', 'best');

    %% 3. Detailed view near optimum
    figure('Name', 'Shifted Coupled Sine Bowl 2D - Near Optimum', 'Position', [200, 200, 1200, 500]);

    % Create zoomed-in grid around optimum
    zoom_range = 2; % ±2 units around optimum
    x1_zoom = linspace(c(1) - zoom_range, c(1) + zoom_range, 200);
    x2_zoom = linspace(c(2) - zoom_range, c(2) + zoom_range, 200);
    [X1_zoom, X2_zoom] = meshgrid(x1_zoom, x2_zoom);

    Z_zoom = zeros(size(X1_zoom));
    for i = 1:numel(X1_zoom)
        x_vec = [X1_zoom(i), X2_zoom(i)];
        Z_zoom(i) = shiftedCoupledSineBowl(x_vec, c, w, alpha, beta, gamma, delta, d);
    end

    % Subplot 1: Zoomed 3D surface
    subplot(1, 2, 1);
    surf(X1_zoom, X2_zoom, Z_zoom, 'EdgeColor', 'none');
    xlabel('x_1'); ylabel('x_2'); zlabel('f(x_1, x_2)');
    title(sprintf('Zoomed Surface (±%.1f around optimum)', zoom_range));
    colorbar;
    view(45, 30);
    hold on;
    plot3(c(1), c(2), shiftedCoupledSineBowl(c, c, w, alpha, beta, gamma, delta, d), ...
        'r*', 'MarkerSize', 20, 'LineWidth', 3);
    % Plot solution point if provided
    if ~isempty(solution_point)
        f_sol = shiftedCoupledSineBowl(solution_point, c, w, alpha, beta, gamma, delta, d);
        plot3(solution_point(1), solution_point(2), f_sol, ...
            'y*', 'MarkerSize', 20, 'LineWidth', 3);
    end
    camlight; lighting gouraud;
    grid on;

    % Subplot 2: Zoomed contour
    subplot(1, 2, 2);
    contourf(X1_zoom, X2_zoom, Z_zoom, 30);
    xlabel('x_1'); ylabel('x_2');
    title(sprintf('Zoomed Contour (±%.1f around optimum)', zoom_range));
    colorbar;
    hold on;
    plot(c(1), c(2), 'r*', 'MarkerSize', 20, 'LineWidth', 3);
    text(c(1)+0.1, c(2)+0.1, sprintf('(%.4f, %.4f)\nf=%.6f', c(1), c(2), ...
        shiftedCoupledSineBowl(c, c, w, alpha, beta, gamma, delta, d)), ...
        'Color', 'red', 'FontSize', 10, 'FontWeight', 'bold', 'BackgroundColor', 'white');
    % Plot solution point if provided
    if ~isempty(solution_point)
        plot(solution_point(1), solution_point(2), 'y*', 'MarkerSize', 20, 'LineWidth', 3);
        f_sol = shiftedCoupledSineBowl(solution_point, c, w, alpha, beta, gamma, delta, d);
        text(solution_point(1)-0.3, solution_point(2)-0.2, sprintf('Sol: (%.4f, %.4f)\nf=%.6e', solution_point(1), solution_point(2), f_sol), ...
            'Color', 'yellow', 'FontSize', 9, 'FontWeight', 'bold', 'BackgroundColor', 'black');
    end
    grid on;
    axis equal;

    %% 4. Gradient and Hessian analysis
    fprintf('\n=== Analysis at Optimum ===\n');
    fprintf('Optimum location: c = [%.6f, %.6f]\n', c(1), c(2));
    fprintf('Optimum value: f(c) = %.10f\n', shiftedCoupledSineBowl(c, c, w, alpha, beta, gamma, delta, d));

    % Numerical gradient at optimum
    h = 1e-8;
    grad = zeros(1, d);
    for i = 1:d
        c_plus = c;
        c_plus(i) = c_plus(i) + h;
        c_minus = c;
        c_minus(i) = c_minus(i) - h;
        grad(i) = (shiftedCoupledSineBowl(c_plus, c, w, alpha, beta, gamma, delta, d) - ...
                   shiftedCoupledSineBowl(c_minus, c, w, alpha, beta, gamma, delta, d)) / (2*h);
    end
    fprintf('Gradient at optimum: [%.2e, %.2e]\n', grad(1), grad(2));

    %% 5. Function characteristics
    fprintf('\n=== Function Characteristics ===\n');

    % Sample points to understand function range
    n_samples = 10000;
    random_samples = rand(n_samples, d) .* 10 - 5; % uniform in [-5, 5]
    f_samples = zeros(n_samples, 1);
    for i = 1:n_samples
        f_samples(i) = shiftedCoupledSineBowl(random_samples(i,:), c, w, alpha, beta, gamma, delta, d);
    end

    fprintf('Statistics from %d random samples in [-5,5]^2:\n', n_samples);
    fprintf('  Mean:   %.4f\n', mean(f_samples));
    fprintf('  Median: %.4f\n', median(f_samples));
    fprintf('  Std:    %.4f\n', std(f_samples));
    fprintf('  Min:    %.4f (at [%.3f, %.3f])\n', min(f_samples), ...
        random_samples(find(f_samples == min(f_samples), 1), 1), ...
        random_samples(find(f_samples == min(f_samples), 1), 2));
    fprintf('  Max:    %.4f\n', max(f_samples));

    % Corners of the domain
    fprintf('\nFunction values at domain corners:\n');
    corners = [-5 -5; -5 5; 5 -5; 5 5];
    for i = 1:size(corners, 1)
        f_corner = shiftedCoupledSineBowl(corners(i,:), c, w, alpha, beta, gamma, delta, d);
        fprintf('  f([%2d, %2d]) = %.4f\n', corners(i,1), corners(i,2), f_corner);
    end

    fprintf('\n=== Visualization Complete! ===\n');
    fprintf('Three figures created showing different aspects of the function.\n');

    % If solution point was provided, show comparison
    if ~isempty(solution_point)
        fprintf('\n=== Solution Point Analysis ===\n');
        f_sol = shiftedCoupledSineBowl(solution_point, c, w, alpha, beta, gamma, delta, d);
        f_opt = shiftedCoupledSineBowl(c, c, w, alpha, beta, gamma, delta, d);

        fprintf('Solution point:      [%.15f, %.15f]\n', solution_point(1), solution_point(2));
        fprintf('Optimum (c):         [%.15f, %.15f]\n', c(1), c(2));
        fprintf('\n');
        fprintf('f(solution):         %.15e\n', f_sol);
        fprintf('f(optimum):          %.15e\n', f_opt);
        fprintf('Distance to optimum: %.15e\n', norm(solution_point - c));
        fprintf('Fitness difference:  %.15e\n', f_sol - f_opt);

        if f_sol < 1e-6
            fprintf('\n✓ Excellent! Solution is very close to optimum.\n');
        elseif f_sol < 1e-3
            fprintf('\n✓ Good! Solution is near optimum.\n');
        elseif f_sol < 1
            fprintf('\n→ Solution is reasonably close to optimum.\n');
        else
            fprintf('\n⚠ Solution is far from optimum.\n');
        end
    end

end

% Define the fitness function (must be at the end of the file)
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
