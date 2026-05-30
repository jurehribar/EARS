% MATLAB script to plot the Smooth Cosine Plateaus function
%
% g(x,y) = cos(x)*cos(y)
%
% SMOOTH version: plateaus follow the level sets of g (not circular),
% giving C1-continuous joins between plateau and cosine surface:
%
%   F(x,y) = 1,              if cos(x)*cos(y) >= c
%           = cos(x)*cos(y), otherwise
%
% Equivalently:  F(x,y) = min(1, g(x,y))  when g <= 1
%
% Domain: [-2*pi, 2*pi] x [-2*pi, 2*pi]  (contains 3x3 = 9 cosine peaks)

%% ADJUSTABLE PARAMETERS
c = 0.9;    % Threshold: plateau where g(x,y) >= c  (try 0.7, 0.8, 0.95)
N = 600;    % Grid resolution

%% Domain
x = linspace(-2*pi, 2*pi, N);
y = linspace(-2*pi, 2*pi, N);
[X, Y] = meshgrid(x, y);

%% Compute g(x,y) and F(x,y)
G = cos(X) .* cos(Y);       % base cosine surface

% F(x,y) = min(g(x,y), c)  →  plateau at height c (where surface naturally reaches c)
Z_display = min(G, c);

%% Plot
figure('Position', [100, 100, 1300, 550]);

%--- Subplot 1: 3D Surface ---
subplot(1, 2, 1);
surf(X, Y, Z_display, 'EdgeColor', 'none');
colormap(parula);
colorbar;
xlabel('x');
ylabel('y');
zlabel('F(x,y)');
title(sprintf('Smooth Cosine Plateaus (c = %.2f) - 3D View', c), ...
    'Interpreter', 'none');
view(-35, 35);
grid on;
axis tight;

% (no threshold plane needed - plateau is flush with the surface)

%--- Subplot 2: Contour plot ---
subplot(1, 2, 2);
contourf(X, Y, Z_display, 40);
colormap(parula);
colorbar;
xlabel('x');
ylabel('y');
title(sprintf('Smooth Cosine Plateaus (c = %.2f) - Contour View', c), ...
    'Interpreter', 'none');
grid on;
axis equal;
xlim([-2*pi, 2*pi]);
ylim([-2*pi, 2*pi]);

% Tick labels in multiples of pi
xticks([-2*pi, -pi, 0, pi, 2*pi]);
xticklabels({'-2\pi', '-\pi', '0', '\pi', '2\pi'});
yticks([-2*pi, -pi, 0, pi, 2*pi]);
yticklabels({'-2\pi', '-\pi', '0', '\pi', '2\pi'});

% Draw the plateau boundary as a contour line at level c
hold on;
contour(X, Y, G, [c c], 'r--', 'LineWidth', 1.8);
hold off;

%% Second figure: side-by-side comparison of original vs smooth version
figure('Position', [100, 700, 1300, 450]);

%--- Original g(x,y) = cos(x)cos(y) ---
subplot(1, 3, 1);
surf(X, Y, G, 'EdgeColor', 'none');
colormap(parula);
colorbar;
xlabel('x'); ylabel('y'); zlabel('g');
title('Original: g(x,y) = cos(x)cos(y)', 'Interpreter', 'none');
view(-35, 35); grid on; axis tight;

%--- Smooth plateau version ---
subplot(1, 3, 2);
surf(X, Y, Z_display, 'EdgeColor', 'none');
colormap(parula);
colorbar;
xlabel('x'); ylabel('y'); zlabel('F');
title(sprintf('Smooth Plateaus: F = min(g, 1) for g>=%.2f', c), ...
    'Interpreter', 'none');
view(-35, 35); grid on; axis tight;

%--- Cross-section along x-axis (y=0) ---
subplot(1, 3, 3);
g_slice  = cos(x);                     % g(x, 0) = cos(x)*cos(0) = cos(x)
F_slice  = min(g_slice, c);

plot(x, g_slice, 'b-',  'LineWidth', 1.5, 'DisplayName', 'g(x,0) = cos(x)');
hold on;
plot(x, F_slice, 'r-',  'LineWidth', 2.0, 'DisplayName', sprintf('F(x,0), c=%.2f',c));
yline(c, 'k--', 'LineWidth', 1.2, 'DisplayName', sprintf('threshold c=%.2f',c));
hold off;
xlabel('x', 'Interpreter', 'none');
ylabel('F', 'Interpreter', 'none');
title('Cross-section along y = 0', 'Interpreter', 'none');
xticks([-2*pi, -pi, 0, pi, 2*pi]);
xticklabels({'-2\pi', '-\pi', '0', '\pi', '2\pi'});
legend('Location', 'best');
grid on;
ylim([-1.1 1.1]);

%% Print info
fprintf('\nSmooth Cosine Plateaus Function Properties:\n');
fprintf('  g(x,y)   = cos(x)*cos(y)\n');
fprintf('  F(x,y)   = c=%.4f      if g(x,y) >= %.4f\n', c, c);
fprintf('  F(x,y)   = cos(x)*cos(y) if g(x,y) <  %.4f\n', c);
fprintf('  Domain: [-2*pi, 2*pi] x [-2*pi, 2*pi]\n');
fprintf('  9 plateau regions (3x3 cosine peaks)\n');
fprintf('  Plateau boundary: level set g(x,y) = %.4f\n', c);
fprintf('  Key property: F is C1-continuous at plateau boundary\n');
fprintf('  (gradient of g matches gradient of plateau=1 only if c=1;\n');
fprintf('   for c<1 the gradient of g is zero only exactly at the peak)\n');
fprintf('\n  Try different thresholds:\n');
fprintf('    c = 0.95 -> small tight plateaus\n');
fprintf('    c = 0.80 -> medium plateaus\n');
fprintf('    c = 0.60 -> large plateaus, nearly touching\n');

