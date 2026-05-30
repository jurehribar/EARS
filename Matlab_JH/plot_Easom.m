% MATLAB script to plot the Easom function (2D)
%
% f(x,y) = -cos(x) * cos(y) * exp(-((x-pi)^2 + (y-pi)^2))
%
% Domain:   x, y in [-100, 100]  (matches Easom.java)
%
% Global minimum: f(pi, pi) = -1
% Reference: https://www.sfu.ca/~ssurjano/easom.html

%% PARAMETERS
N = 600;   % grid resolution

%% Domain - full range (matches Easom.java)
x_zoom = linspace(-100, 100, N);
y_zoom = linspace(-100, 100, N);
[X_zoom, Y_zoom] = meshgrid(x_zoom, y_zoom);

%% Domain - tight view around the minimum
x_tight = linspace(0, 2*pi, N);
y_tight = linspace(0, 2*pi, N);
[X_tight, Y_tight] = meshgrid(x_tight, y_tight);

%% Evaluate function
easom = @(X, Y) -cos(X) .* cos(Y) .* exp(-((X - pi).^2 + (Y - pi).^2));

Z_zoom  = easom(X_zoom,  Y_zoom);
Z_tight = easom(X_tight, Y_tight);

%% ── Figure 1: Zoomed view [-10, 10] ─────────────────────────────────────────
figure('Position', [100, 100, 1300, 550]);

% Subplot 1: 3D surface (zoomed)
subplot(1, 2, 1);
surf(X_zoom, Y_zoom, Z_zoom, 'EdgeColor', 'none');
colormap(jet);
colorbar;
xlabel('x');
ylabel('y');
zlabel('f(x,y)');
title('Easom Function [-100,100] - 3D View', 'Interpreter', 'none');
view(-35, 35);
grid on;
axis tight;

% Mark global minimum at (pi, pi)
hold on;
plot3(pi, pi, -1.0, 'y*', 'MarkerSize', 18, 'LineWidth', 2.5);
hold off;

% Subplot 2: Contour (zoomed)
subplot(1, 2, 2);
contourf(X_zoom, Y_zoom, Z_zoom, 40);
colormap(jet);
colorbar;
xlabel('x');
ylabel('y');
title('Easom Function [-100,100] - Contour View', 'Interpreter', 'none');
grid on;
axis equal;
xlim([-100 100]);
ylim([-100 100]);

hold on;
plot(pi, pi, 'y*', 'MarkerSize', 18, 'LineWidth', 2.5);
text(pi + 0.3, pi + 0.5, sprintf('(\\pi, \\pi)\nf = -1'), ...
    'Color', 'white', 'FontSize', 10, 'FontWeight', 'bold');
hold off;

%% ── Figure 2: Tight view around minimum [0, 2*pi] ───────────────────────────
figure('Position', [100, 700, 1300, 550]);

% Subplot 1: 3D surface (tight)
subplot(1, 2, 1);
surf(X_tight, Y_tight, Z_tight, 'EdgeColor', 'none');
colormap(parula);
colorbar;
xlabel('x');
ylabel('y');
zlabel('f(x,y)');
title('Easom Function [0, 2\pi] - 3D View (around minimum)');
view(-35, 35);
grid on;
axis tight;

% Mark global minimum
hold on;
plot3(pi, pi, -1.0, 'y*', 'MarkerSize', 18, 'LineWidth', 2.5);
hold off;

xticks([0, pi/2, pi, 3*pi/2, 2*pi]);
xticklabels({'0', '\pi/2', '\pi', '3\pi/2', '2\pi'});
yticks([0, pi/2, pi, 3*pi/2, 2*pi]);
yticklabels({'0', '\pi/2', '\pi', '3\pi/2', '2\pi'});

% Subplot 2: Contour (tight)
subplot(1, 2, 2);
contourf(X_tight, Y_tight, Z_tight, 40);
colormap(parula);
colorbar;
xlabel('x');
ylabel('y');
title('Easom Function [0, 2\pi] - Contour View');
grid on;
axis equal;
xlim([0, 2*pi]);
ylim([0, 2*pi]);

xticks([0, pi/2, pi, 3*pi/2, 2*pi]);
xticklabels({'0', '\pi/2', '\pi', '3\pi/2', '2\pi'});
yticks([0, pi/2, pi, 3*pi/2, 2*pi]);
yticklabels({'0', '\pi/2', '\pi', '3\pi/2', '2\pi'});

hold on;
plot(pi, pi, 'y*', 'MarkerSize', 18, 'LineWidth', 2.5);
text(pi + 0.1, pi + 0.25, sprintf('(\\pi, \\pi)\nf = -1'), ...
    'Color', 'white', 'FontSize', 10, 'FontWeight', 'bold');
hold off;

%% ── Figure 3: Cross-sections ─────────────────────────────────────────────────
figure('Position', [100, 400, 1300, 400]);

% Cross-section along x (y = pi)
subplot(1, 2, 1);
x_cs = linspace(-100, 100, 2000);
f_cs = -cos(x_cs) .* cos(pi) .* exp(-((x_cs - pi).^2));
plot(x_cs, f_cs, 'b-', 'LineWidth', 2);
xlabel('x');
ylabel('f(x, \pi)');
title('Cross-section: f(x, \pi) = -cos(x) \cdot exp(-(x-\pi)^2)');
xline(pi, 'r--', '\pi', 'LineWidth', 1.5, 'LabelVerticalAlignment', 'bottom');
grid on;
ylim([-1.1 0.5]);

% Cross-section along diagonal (x = y)
subplot(1, 2, 2);
t = linspace(-100, 100, 2000);
f_diag = -cos(t).^2 .* exp(-2*(t - pi).^2);
plot(t, f_diag, 'b-', 'LineWidth', 2);
xlabel('t');
ylabel('f(t, t)');
title('Diagonal cross-section: f(t, t) = -cos^2(t) \cdot exp(-2(t-\pi)^2)');
xline(pi, 'r--', '\pi', 'LineWidth', 1.5, 'LabelVerticalAlignment', 'bottom');
grid on;
ylim([-1.1 0.5]);

%% Print info
fprintf('\nEasom Function Properties:\n');
fprintf('  f(x,y) = -cos(x) * cos(y) * exp(-((x-pi)^2 + (y-pi)^2))\n');
fprintf('  Domain:  x, y in [-100, 100]\n');
fprintf('  Global minimum: f(pi, pi) = -1\n');
fprintf('    where pi = %.6f\n', pi);
fprintf('  f(0, 0) = %.6f  (value at origin)\n', easom(0, 0));
fprintf('  The function is nearly flat everywhere except near (pi, pi)\n');
fprintf('  Making it very hard for optimization algorithms to find the minimum\n');

