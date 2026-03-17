# EARS - Implemented Algorithms List

## Single-Objective (SO) Optimization Algorithms

### Differential Evolution (DE) Family
1. **DE** - Differential Evolution
   - Multiple strategies available:
     - DE/best/1/exp
     - DE/rand/1/exp
     - DE/rand-to-best/1/exp
     - DE/best/2/exp
     - DE/rand/2/exp
     - DE/best/1/bin
     - DE/rand/1/bin
     - DE/rand-to-best/1/bin
     - DE/best/2/bin
     - DE/rand/2/bin
     - JDE/rand/1/bin (Self-adaptive jDE)

2. **JADE** - Adaptive Differential Evolution With Optional External Archive

3. **ADE** - Advanced Differential Evolution

4. **jDElscop** - jDE with linear population size reduction and three strategies

5. **LSHADE** - Linear Population Size Reduction Success-History based Adaptive DE

6. **jSO** - jSO (Success-History based Adaptive DE variant)

### Evolution Strategies (ES)
7. **ES(1+1)** - Evolution Strategy (1+1) with 1/5 rule

8. **ES(1+N)** - Evolution Strategy (1+N)

9. **ES(1,N)** - Evolution Strategy (1,N)

### Particle Swarm Optimization (PSO) Family
10. **PSO** - Particle Swarm Optimization

11. **PSOv2** - Particle Swarm Optimization Version 2

12. **PSOomega** - Particle Swarm Optimization Omega

### Swarm Intelligence Algorithms
13. **ABC** - Artificial Bee Colony

14. **FA** - Firefly Algorithm

15. **BA** - Bat Algorithm

16. **CS** - Cuckoo Search

17. **FPA** - Flower Pollination Algorithm

18. **FWA** - Fireworks Algorithm for Optimization

19. **FSS** - Fish School Search

20. **MBF** - Mouth Brooding Fish

21. **SSA** - Salp Swarm Algorithm

22. **GOA** - Grasshopper Optimisation Algorithm

23. **GAOA** - Gazelle Optimisation Algorithm

24. **WOA** - Whale Optimization Algorithm

25. **PDWOA** - Pbest-guided Differential Whale Optimization Algorithm

26. **GWO** - Gray Wolf Optimizer

27. **AO** - Aquila Optimizer

28. **MFO** - Moth Flame Optimization

29. **MRFO** - Manta Ray Foraging Optimization

30. **HBA** - Honey Badger Algorithm

31. **AVOA** - African Vultures Optimization Algorithm

32. **RSA** - Reptile Search Algorithm

33. **SMA** - Slime Mould Algorithm

34. **SCSO** - Sand Cat Swarm Optimizer

### Physics and Chemistry Inspired
35. **GSA** - Gravitational Search Algorithm

36. **GSAv2** - Gravitational Search Algorithm Version 2

37. **MVO** - Multi-Verse Optimizer

38. **BFO** - Bacterial Foraging Optimization

39. **CRO** - Coral Reefs Optimization

40. **ERSA** - Electron Radar Search Algorithm

### Socio-Inspired Algorithms
41. **ICA** - Imperialist Competitive Algorithm

42. **TLBO** - Teaching Learning Based Optimization

43. **OSSTLBO** - Opposite Stopping Swarm Teaching–learning-Based Optimization

44. **BTLBO** - Balanced Teaching-Learning-Based Optimization

45. **LaF** - Leaders and Followers

### Self-Organizing Algorithms
46. **SOMA** - Self-Organizing Migrating Algorithm
   - Strategies available:
     - ALL_TO_ALL
     - ALL_TO_ALL_ADAPTIVE
     - ALL_TO_ONE
     - ALL_TO_ONE_RANDOM

### Classical Optimization Algorithms
47. **SA** - Simulated Annealing

48. **HC** - Hill Climbing
   - Strategies available:
     - ANY_ASCENT
     - STEEPEST_ASCENT
     - RANDOM_RESTART

49. **SGD** - Stochastic Gradient Descent

### Miscellaneous Algorithms
50. **FDO** - Fitness Dependent Optimizer

51. **RMO** - Radial Movement Optimization

52. **HSA** - Harmony Search Algorithm

53. **ISCA** - Improved Sine Cosine Algorithm

54. **GNDO** - Generalized Normal Distribution Optimization

55. **AHA** - Artificial Hummingbird Algorithm

56. **AAA** - Artificial Algae Algorithm

57. **CMAES** - Covariance Matrix Adaptation Evolutionary Strategy

### Random/Baseline Algorithms
58. **RS** - Random Search

59. **RWAM** - Random Walk Arithmetic

---

## Summary Statistics
- **Total Single-Objective Algorithms**: 59+ (including strategy variants)
- **Differential Evolution variants**: 6
- **Evolution Strategies**: 3
- **PSO variants**: 3
- **Swarm Intelligence**: 20+
- **Physics/Chemistry based**: 7
- **Socio-inspired**: 5
- **Classical methods**: 3

## Most Popular Algorithm Families
1. **Differential Evolution (DE)** - Highly versatile with many strategies
2. **Swarm Intelligence** - Largest category with bio-inspired algorithms
3. **Evolution Strategies (ES)** - Classic evolutionary computation
4. **Teaching-Learning Based** - Multiple variants available

## Notes:
- Algorithms with multiple strategies (DE, SOMA, HC) can be instantiated with different configurations
- Many algorithms support parameter tuning through AlgorithmParameter annotations
- Most algorithms are nature-inspired metaheuristics
- The framework includes both classic (SA, HC) and modern (recent 2020s) algorithms

