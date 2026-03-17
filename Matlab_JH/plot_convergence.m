function plot_convergence(filename)
% PLOT_CONVERGENCE - Plot optimization convergence from EARS results file
%
% Usage:
%   plot_convergence('results.txt')
%   plot_convergence('/path/to/results.txt')
%
% Input:
%   filename - Path to the results file from EARS SOSingleRun
%
% The results file format expected:
%   iteration objectives: [scientific_notation (actual_value)] variables: [...] F:... CR:...
%
% Example:
%   60 objectives: [3,02e1 (30.19971866193585)] variables: [...]

    % Check if filename argument is provided
    if nargin < 1
        error('Please provide a filename as input parameter.\nUsage: plot_convergence(''results.txt'')');
    end

    % Check if file exists
    if ~exist(filename, 'file')
        error('File not found: %s', filename);
    end

    fprintf('Reading results from: %s\n', filename);

    % Open and read the file
    fid = fopen(filename, 'r');
    if fid == -1
        error('Could not open file: %s', filename);
    end

    % Initialize arrays
    iterations = [];
    fitness_values = [];

    % Read file line by line
    line_count = 0;
    while ~feof(fid)
        line = fgetl(fid);
        if ischar(line)
            line_count = line_count + 1;

            % Parse the line
            % Format: iteration objectives: [sci_notation (actual_value)] ...
            % The actual value in parentheses can have E or e for scientific notation
            tokens = regexp(line, '(\d+)\s+objectives:\s+\[[\d,e+-]+\s+\(([\d.eE+-]+)\)\]', 'tokens');

            if ~isempty(tokens)
                iteration = str2double(tokens{1}{1});
                fitness = str2double(tokens{1}{2});

                iterations = [iterations; iteration];
                fitness_values = [fitness_values; fitness];
            end
        end
    end

    fclose(fid);

    fprintf('Successfully read %d data points.\n', length(iterations));

    if isempty(iterations)
        error('No valid data found in the file. Please check the file format.');
    end

    % Create the main figure
    figure('Name', 'Optimization Convergence Analysis', 'Position', [100, 100, 1400, 900]);

    %% Subplot 1: Linear scale convergence plot
    subplot(2, 3, 1);
    plot(iterations, fitness_values, 'b-', 'LineWidth', 2);
    hold on;
    plot(iterations, fitness_values, 'ro', 'MarkerSize', 4, 'MarkerFaceColor', 'r');
    xlabel('Iteration (Function Evaluations)');
    ylabel('Fitness Value');
    title('Convergence Plot (Linear Scale)');
    grid on;

    % Mark first and last points
    plot(iterations(1), fitness_values(1), 'gs', 'MarkerSize', 12, 'LineWidth', 2);
    plot(iterations(end), fitness_values(end), 'ms', 'MarkerSize', 12, 'LineWidth', 2);

    legend('Convergence', 'Data points', 'Start', 'End', 'Location', 'best');

    %% Subplot 2: Log scale convergence plot
    subplot(2, 3, 2);
    semilogy(iterations, fitness_values, 'b-', 'LineWidth', 2);
    hold on;
    semilogy(iterations, fitness_values, 'ro', 'MarkerSize', 4, 'MarkerFaceColor', 'r');
    xlabel('Iteration (Function Evaluations)');
    ylabel('Fitness Value (log scale)');
    title('Convergence Plot (Log Scale)');
    grid on;

    % Mark first and last points
    semilogy(iterations(1), fitness_values(1), 'gs', 'MarkerSize', 12, 'LineWidth', 2);
    semilogy(iterations(end), fitness_values(end), 'ms', 'MarkerSize', 12, 'LineWidth', 2);

    legend('Convergence', 'Data points', 'Start', 'End', 'Location', 'best');

    %% Subplot 3: Improvement per iteration
    subplot(2, 3, 3);
    improvements = [0; -diff(fitness_values)]; % Negative diff because we're minimizing
    bar(iterations, improvements);
    xlabel('Iteration (Function Evaluations)');
    ylabel('Improvement');
    title('Improvement per Iteration');
    grid on;

    % Add horizontal line at zero
    hold on;
    plot([iterations(1), iterations(end)], [0, 0], 'r--', 'LineWidth', 1.5);

    %% Subplot 4: Fitness value histogram
    subplot(2, 3, 4);
    histogram(fitness_values, 30, 'FaceColor', 'b', 'EdgeColor', 'k');
    xlabel('Fitness Value');
    ylabel('Frequency');
    title('Distribution of Fitness Values');
    grid on;

    %% Subplot 5: Convergence rate (moving average)
    subplot(2, 3, 5);
    window_size = min(5, floor(length(fitness_values)/3));
    if window_size >= 1
        smoothed = movmean(fitness_values, window_size);
        plot(iterations, fitness_values, 'b-', 'LineWidth', 1, 'Color', [0.7, 0.7, 1]);
        hold on;
        plot(iterations, smoothed, 'r-', 'LineWidth', 2);
        xlabel('Iteration (Function Evaluations)');
        ylabel('Fitness Value');
        title(sprintf('Convergence with Moving Average (window=%d)', window_size));
        legend('Raw data', 'Moving average', 'Location', 'best');
        grid on;
    else
        plot(iterations, fitness_values, 'b-', 'LineWidth', 2);
        xlabel('Iteration (Function Evaluations)');
        ylabel('Fitness Value');
        title('Convergence Plot');
        grid on;
    end

    %% Subplot 6: Statistics text box
    subplot(2, 3, 6);
    axis off;

    % Calculate statistics
    initial_fitness = fitness_values(1);
    final_fitness = fitness_values(end);
    best_fitness = min(fitness_values);
    worst_fitness = max(fitness_values);
    total_improvement = initial_fitness - final_fitness;
    improvement_percent = (total_improvement / initial_fitness) * 100;

    % Find iteration where best fitness was achieved
    [~, best_idx] = min(fitness_values);
    best_iteration = iterations(best_idx);

    % Count number of improvements
    num_improvements = sum(improvements > 0);

    % Create statistics text
    stats_text = {
        '\bf\fontsize{12}Optimization Statistics'
        ' '
        sprintf('\\bfTotal Iterations:\\rm %d', length(iterations))
        sprintf('\\bfIterations Range:\\rm %d - %d', iterations(1), iterations(end))
        ' '
        sprintf('\\bfInitial Fitness:\\rm %.6e', initial_fitness)
        sprintf('\\bfFinal Fitness:\\rm %.6e', final_fitness)
        sprintf('\\bfBest Fitness:\\rm %.6e', best_fitness)
        sprintf('  \\rm(at iteration %d)', best_iteration)
        ' '
        sprintf('\\bfTotal Improvement:\\rm %.6e', total_improvement)
        sprintf('\\bfImprovement:\\rm %.2f%%', improvement_percent)
        ' '
        sprintf('\\bfNumber of Improvements:\\rm %d / %d', num_improvements, length(improvements)-1)
        sprintf('\\bfAverage Fitness:\\rm %.6e', mean(fitness_values))
        sprintf('\\bfStd Deviation:\\rm %.6e', std(fitness_values))
    };

    text(0.1, 0.9, stats_text, 'Units', 'normalized', ...
        'VerticalAlignment', 'top', 'FontSize', 10, 'Interpreter', 'tex');

    % Add a box
    rectangle('Position', [0.05, 0.05, 0.9, 0.9], 'EdgeColor', 'k', 'LineWidth', 2);

    %% Print summary to console
    fprintf('\n=== Optimization Summary ===\n');
    fprintf('Total iterations: %d (from %d to %d)\n', length(iterations), iterations(1), iterations(end));
    fprintf('Initial fitness:  %.6e\n', initial_fitness);
    fprintf('Final fitness:    %.6e\n', final_fitness);
    fprintf('Best fitness:     %.6e (at iteration %d)\n', best_fitness, best_iteration);
    fprintf('Total improvement: %.6e (%.2f%%)\n', total_improvement, improvement_percent);
    fprintf('Number of improvements: %d / %d iterations\n', num_improvements, length(improvements)-1);

    %% Create a second figure with zoomed views
    figure('Name', 'Detailed Convergence Views', 'Position', [150, 150, 1200, 600]);

    % First half of optimization
    subplot(1, 3, 1);
    mid_point = ceil(length(iterations) / 2);
    plot(iterations(1:mid_point), fitness_values(1:mid_point), 'b-', 'LineWidth', 2);
    hold on;
    plot(iterations(1:mid_point), fitness_values(1:mid_point), 'ro', 'MarkerSize', 5);
    xlabel('Iteration');
    ylabel('Fitness Value');
    title('First Half of Optimization');
    grid on;

    % Second half of optimization
    subplot(1, 3, 2);
    plot(iterations(mid_point:end), fitness_values(mid_point:end), 'b-', 'LineWidth', 2);
    hold on;
    plot(iterations(mid_point:end), fitness_values(mid_point:end), 'ro', 'MarkerSize', 5);
    xlabel('Iteration');
    ylabel('Fitness Value');
    title('Second Half of Optimization');
    grid on;

    % Last 20% of optimization (fine-tuning phase)
    subplot(1, 3, 3);
    last_20_percent = max(1, ceil(0.8 * length(iterations)));
    plot(iterations(last_20_percent:end), fitness_values(last_20_percent:end), 'b-', 'LineWidth', 2);
    hold on;
    plot(iterations(last_20_percent:end), fitness_values(last_20_percent:end), 'ro', 'MarkerSize', 5);
    xlabel('Iteration');
    ylabel('Fitness Value');
    title('Last 20% of Optimization (Fine-tuning)');
    grid on;

    fprintf('\n=== Plots Created Successfully ===\n');
    fprintf('Two figures created:\n');
    fprintf('  1. Optimization Convergence Analysis (6 subplots)\n');
    fprintf('  2. Detailed Convergence Views (3 subplots)\n');
end

