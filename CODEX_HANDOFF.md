# Codex handoff: MDE exploration/exploitation experiment

## Repository and current state

- Primary repository: `C:\dev\EARS`
- Reference/older repository inspected during this chat: `C:\dev\exploration-and-exploitation`
- Current date at handoff: 2026-07-24
- Current EARS `HEAD`: `5d90f14d` — **Parent in MDE from multiple to only target**
- The tracked worktree is clean at the time of handoff.
- `docs/` and `outputs/` are untracked. They pre-existed the handoff work and must not be deleted, modified, or attributed to this chat without inspecting them first.

## Objective

The long-term objective is to reproduce for Memetic Differential Evolution (MDE) the exploration/exploitation experiment previously performed for Artificial Bee Colony (ABC), using the exploration/exploitation and attraction-basin machinery ported from the older `exploration-and-exploitation` project into EARS.

The immediate completed objective was narrower: correct MDE ancestry logging so a DE trial has one reference parent—the target vector it competes against—rather than the three mutation contributors.

The user initially requested a high-level-to-detailed walkthrough and explicitly said not to write code. Later, the user explicitly authorized the MDE logging change. Do not infer authorization for additional experiment implementation from the earlier walkthrough request; discuss or implement further work only in response to the user's next instruction.

## Professor's requested research work

The professor's email (in Slovenian) asks for the following:

1. Explain the statement that, with only 20,000 function evaluations, using five elite individuals per generation eliminates MDE's advantage.
2. Explain how the implemented gradient descent with Armijo backtracking differs from the local search currently used elsewhere, and identify relevant literature.
3. Avoid relying only on deterministic local-search scheduling every `q` generations over `k` individuals. Use the team's exploration/exploitation measures to decide when and on whom to run local search.
4. Read/use the article: <https://www.mdpi.com/2227-7390/14/9/1406>.
5. For Rastrigin, `D=10`, with `DE/rand/best/1/bin` (wording in the email; likely compare rand/1/bin and best/1/bin), investigate whether MDE can accelerate the long periods of successful exploitation.
6. For `DE/rand/1/bin`, investigate whether MDE helps with failed exploration and unsuccessful exploitation.
7. Include `DE/best/1/bin` in MDE.
8. Exploitation should begin after 500 function evaluations.
9. Reproduce/improve the behavior corresponding to Figure 50 and unsuccessful exploitation in Figure 158a.

The exact source/plots for Figure 50 and Figure 158a were not supplied in this chat. Locate them before claiming the requested figures have been reproduced.

## Key conceptual decision: what is the DE “parent”?

DE has asymmetric roles:

- Mutation contributors for `DE/rand/1`: `r1`, `r2`, `r3`.
- Crossover sources: the mutant vector and target `x_i`.
- Selection/replacement parent: target `x_i`.
- Complete causal ancestry: target plus the mutation contributors.

For Chen-style exploration/exploitation classification, the reference solution should be the **selection target**, because the generated trial is accepted or rejected by comparison with that target:

```text
target x_i  ---- mutation/crossover ----> trial u_i
     \____________________________________/
                  greedy selection
```

Thus:

```text
exploitation: basin(u_i) == basin(x_i)
exploration:  basin(u_i) != basin(x_i)
```

Successful exploration, failed exploration, deceptive exploration, and successful rejection also depend on the trial-versus-target fitness comparison. Mutation contributors are relevant to provenance, but they are not the replacement reference used by selection.

ABC is simpler because its source solution is both the main generation source and the solution against which the candidate competes. A neighboring food source influences the perturbation but is not the selection parent.

The closest-attractor selection in `EERunAnalyzer` is operationally meaningful when multiple candidate parents are logged, but closeness does not prove common attraction-basin lineage. With MDE now logging one target, closest-parent selection is trivial for MDE's DE phase: the only candidate is the target.

### Literature discussed

- R. Storn and K. Price, “Differential Evolution – A Simple and Efficient Heuristic for Global Optimization over Continuous Spaces,” 1997: <https://doi.org/10.1023/A:1008202821328>
- J. Montgomery et al., “Some Convergence Properties of Differential Evolution”: <https://www.yorku.ca/sychen/research/workshops/CEC2021_Workshop_on_Selection_DE.pdf>
  - This paper describes each `x_i` as the target for replacement.
  - Its exploration/exploitation and failed-exploration definitions use the stored reference solution against which the new search solution is selected.
- S. Chen et al., “An Analysis on the Effect of Selection on Exploration in Particle Swarm Optimization and Differential Evolution,” CEC 2019, pp. 3037–3044.
- The article specified by the professor: <https://www.mdpi.com/2227-7390/14/9/1406>

No article was found whose primary research question is explicitly “which DE input should be called the parent for attraction-basin classification.” The literature instead distinguishes target/reference/selection roles from mutation contributors.

## Evidence from the older `exploration-and-exploitation` repository

Relevant files:

- `C:\dev\exploration-and-exploitation\src\org\um\feri\ears\experiment\ee\DEAlgorithmLogging.java`
- `C:\dev\exploration-and-exploitation\src\org\um\feri\ears\experiment\ee\DEAlgorithmLoggingOldNew.java`
- `C:\dev\exploration-and-exploitation\src\org\um\feri\analyse\EE\experimet\ears\EARSExploreGraphsExplTypeChenTableOnly2.java`

Findings:

- `DEAlgorithmLogging.java` logs mutation contributors. For `DE/rand/1/bin`, it logs `pold[r1]`, `pold[r2]`, and `pold[r3]`.
- `DEAlgorithmLoggingOldNew.java` consistently logs only `pold[i]`, the old target, for every DE strategy.
- Multiple-parent DE logging was added on 2017-03-09 in commit `a2f1c4d`, author recorded as `jjerebic`, commit message:
  - `Added DE Logging, sample calculation of logging for 5 function and added CD calculation for function.`
- `DEAlgorithmLoggingOldNew.java` was added later on 2025-11-12 in commit `67cd8eb`, author recorded as `jernejjereb`, commit message:
  - `latest changes`
- The same 2025 commit added `OldNew` variants for JADE, PSO, and TLBO and added/changed Chen-oriented analysis code.
- `jjerebic` and `jernejjereb` appear to be Jernej Jerebic, but this identity equivalence is an inference from the repository metadata.

The user knows Jernej and intends to email him. Suggested question:

> I noticed that `DEAlgorithmLogging.java` logs the mutation contributors `r1`, `r2`, and `r3`, while the later `DEAlgorithmLoggingOldNew.java` logs only the target `pold[i]`. Was `OldNew` introduced because Chen's exploration/exploitation classification should use the old target that the trial competes against rather than the mutation contributors? Does “OldNew” mean the transition from the old target to the new trial?

Do not present this intent as confirmed until Jernej replies. The timing and code strongly support the interpretation, but the commit message does not document the motivation.

## MDE code walkthrough and behavior

### Main MDE implementation

File:

- `src/org/um/feri/ears/algorithms/so/de/mde/MDE.java`

High-level generation flow:

1. Initialize and evaluate the population.
2. For every target `pold[i]`, select three distinct indices `r1`, `r2`, and `r3`, all different from `i`.
3. Copy the target into the trial array.
4. Perform binomial crossover:
   - `DE/rand/1/bin`: `r1 + F * (r2 - r3)`
   - `DE/best/1/bin`: `bestIt + F * (r2 - r3)`
5. Each coordinate uses the mutant when `random < CR`.
6. The final visited coordinate is always taken from the mutant (`L == D - 1`) so at least one coordinate changes.
7. Repair infeasible coordinates with `task.problem.makeFeasible`.
8. Create and evaluate the trial.
9. Greedily select the trial only when it is strictly better than `pold[i]`; otherwise retain the target.
10. Swap `pold` and `pnew` after processing the generation.
11. At configured intervals, run local search on the top `eliteSize` members.

Supported MDE DE strategies are currently:

- `DE.Strategy.DE_RAND_1_BIN`
- `DE.Strategy.DE_BEST_1_BIN`

The constructor rejects other DE strategies.

Important implementation detail: selection is strict (`isFirstBetter`), not `<=`. Standard DE is frequently written with `<=` so equal-fitness trials may replace targets, but in this EARS implementation equality retains the target. This can matter for plateaus, duplicate vectors, diversity, and how equal-fitness cases are classified. The MDE class comment now shows `<`, matching the implementation.

### Why force at least one mutant coordinate?

Without the forced coordinate, all `D` crossover draws could reject the mutant with probability `(1 - CR)^D`, producing a trial identical to the target. Forcing one mutant coordinate ensures every trial includes some mutation influence and avoids spending an evaluation on an unchanged copy (subject to repair or coincident values).

### Feasibility repair

After mutation/crossover, a coordinate may fall outside the problem's allowed bounds. `makeFeasible(tmp[j], j)` maps/repairs it into the valid domain before objective evaluation.

### Trial evaluation

`task.eval(trial)` computes the objective, increments the function-evaluation accounting, checks the stopping mechanism, and—when ancestry logging is enabled—places the evaluated solution into the ancestry log with the parent information assigned before the call.

### Local search

Files:

- `src/org/um/feri/ears/algorithms/so/de/mde/LocalSearch.java`
- `src/org/um/feri/ears/algorithms/so/de/mde/GradientDescentLocalSearch.java`

The default local search:

- Estimates gradients with central finite differences, requiring forward and backward objective evaluations per dimension.
- Uses gradient descent.
- Uses Armijo backtracking to reduce the step until a sufficient-decrease condition is met.
- Repairs each candidate before evaluation.
- Logs the immediate current solution as the single parent for local-search probe/candidate evaluations when ancestry logging is enabled.

This is expensive because gradient estimation alone costs approximately `2D` evaluations per gradient step, before Armijo backtracking evaluations.

### Analyzer

Files:

- `src/org/um/feri/ears/experiment/ee/EERunAnalyzer.java`
- `src/org/um/feri/ears/experiment/ee/ExplorationType.java`
- `src/org/um/feri/ears/experiment/ee/AttractorLocalSearch.java`
- `src/org/um/feri/ears/experiment/ee/EELogNode.java`

`EERunAnalyzer`:

1. Runs attraction-basin local search for every logged node.
2. If several candidate parents exist, chooses the parent whose attractor is closest to the child's attractor.
3. Compares child/parent basin identity, current fitness, and attractor fitness.
4. Classifies nodes as:
   - `SUCCESSFUL_EXPLORATION`
   - `SUCCESSFUL_REJECTION`
   - `DECEPTIVE_EXPLORATION`
   - `FAILED_EXPLORATION`
   - `SUCCESSFUL_EXPLOITATION`
   - `UNSUCCESSFUL_EXPLOITATION`
   - `INITIAL`

The analysis is expensive and currently post hoc. Before designing an online adaptive MDE trigger, determine which exploration/exploitation signals are actually available online without running attraction-basin local search on every evaluation.

## Completed code change

Commit:

- `5d90f14d` — `Parent in MDE from multiple to only target`
- Author: Jure Hribar
- Date: 2026-07-22 17:40:43 +0200

Files changed:

### `src/org/um/feri/ears/algorithms/so/de/mde/MDE.java`

The DE-phase logging hook now receives the target:

```java
assignTrialParents(trial, pold[i]);
```

The protected hook signature is now:

```java
protected void assignTrialParents(NumberSolution<Double> trial,
                                  NumberSolution<Double> target)
```

### `src/org/um/feri/ears/algorithms/so/de/mde/MDELogging.java`

The override records exactly one parent:

```java
trial.parents = parents(target);
```

The algorithm description now says `target-parent` rather than `mutation-parent`.

### Scope decision

`DELogging.java` was initially changed during the chat, but the user clarified that only MDE should change. All `DELogging.java` edits were then fully reverted. It has no diff and retains its existing multi-contributor logging. Do not change it unless the user separately asks to change the general DE experiment.

Local-search ancestry was intentionally left unchanged: each local-search evaluation records its immediate source/current solution as one parent.

## Commands run and verification

Repository inspection:

```powershell
git status --short
git diff --check
git diff -- src/org/um/feri/ears/algorithms/so/de/mde/MDE.java src/org/um/feri/ears/algorithms/so/de/mde/MDELogging.java
rg -n "assignTrialParents|target-parent|parents\(target\)" src/org/um/feri/ears/algorithms/so/de/mde
git log -5 --date=iso --format="%h %ad %an %s"
git blame -L 200,212 -- src/org/um/feri/ears/algorithms/so/de/mde/MDE.java
git blame -L 53,66 -- src/org/um/feri/ears/algorithms/so/de/mde/MDELogging.java
```

Compilation:

```powershell
.\gradlew.bat compileJava
```

The first sandboxed attempt failed because Gradle tried to download `gradle-8.11-bin.zip` and network access was denied. It was rerun with approved elevated network access. Results:

```text
> Task :compileKotlin
> Task :compileJava
BUILD SUCCESSFUL
```

After narrowing the change back to MDE only, compilation was run again and succeeded in approximately 3 seconds.

No end-to-end MDE exploration/exploitation experiment and no focused automated ancestry assertion test were run in this chat. Compilation is the only completed validation beyond code inspection and Git diff checks.

## Experiment infrastructure already present

Files:

- `src/org/um/feri/ears/experiment/ee/EEExperimentRunner.java`
- `src/org/um/feri/ears/experiment/ee/EEAnalysisRunner.java`
- `src/org/um/feri/ears/experiment/ee/EEAlgorithm.java`
- `src/org/um/feri/ears/experiment/ee/OldCsvAncestorSaver.java`
- `src/org/um/feri/ears/experiment/ee/EERunLogReader.java`
- `src/org/um/feri/ears/experiment/ee/EEProblemFactory.java`
- `src/org/um/feri/ears/experiment/ee/EEMetrics.java`

`EEExperimentRunner` enables ancestry logging, executes an `EEAlgorithm`, and saves ancestry CSV files. It supports:

- an eight-argument single-run mode for a selected algorithm;
- a seven-argument legacy ABC-only mode;
- a default parameter sweep.

At handoff, `EEAlgorithm` contains only:

- `ABC`
- `DE_RAND_1_BIN`

It does **not** yet expose `MDELogging`, so the existing runner cannot directly launch the requested MDE experiment without extending the enum/runner or adding a focused MDE experiment runner.

Default sweep parameters currently include:

- dimensions `{5, 10, 30}`;
- population sizes `{24, 50, 100}`;
- function-evaluation budgets `{50_000, 100_000, 250_000}`;
- 10 repetitions.

These defaults do not include the professor's explicitly mentioned 20,000-evaluation condition or exploitation start at 500 evaluations.

## Unresolved issues and risks

1. **Jernej's intent is unconfirmed.** Wait for the user's email response before stating why `OldNew` was created.
2. **No MDE entry exists in `EEAlgorithm`.** The experiment pipeline currently runs ABC and DE logging, not MDE logging.
3. **Experiment configuration is incomplete.** Exact population size, `F`, `CR`, number of repetitions, local-search settings, random seeds, and fair-evaluation accounting still need to be fixed.
4. **The 500-FE requirement is not implemented as an adaptive trigger.** Clarify whether local search must be completely disabled before evaluation 500, or whether 500 is only the start of analysis.
5. **Post-hoc versus online information.** Current basin classification requires expensive local searches on logged solutions. An adaptive MDE cannot simply call this analyzer online without changing the evaluation budget and algorithm behavior.
6. **Fair accounting for local search.** All gradient probes and Armijo trials consume function evaluations. Comparisons must use the same total budget.
7. **Strict versus non-strict selection.** MDE currently accepts only strictly better trials. Decide whether experiments should match this implementation or canonical DE's commonly written `<=`.
8. **Equal-fitness classification.** In `EERunAnalyzer`, different-basin equal-fitness child/parent cases fall through to `INITIAL`; same-basin equal-fitness cases become `UNSUCCESSFUL_EXPLOITATION`. Verify that this is intended.
9. **“Closest parent” remains in the generic analyzer.** It is harmless for new MDE DE-phase nodes with one candidate, but still affects algorithms that log multiple candidates.
10. **Best-vector identity.** In `DE/best/1/bin`, the target remains the selection parent even though `bestIt` is the mutant base. This is intentional under the selected reference definition.
11. **Figures 50 and 158a are unavailable here.** Locate the source document/data and confirm axes, colors, categories, and experimental parameters.
12. **No reproducibility check yet.** Determine how EARS seeds `RNG` and whether common initial populations/seeds are supported.

## Exact recommended next steps

1. **Receive and record Jernej's answer.**
   - Confirm the intended meaning of `OldNew`.
   - Confirm whether target-only logging was introduced specifically for Chen-style classification.
   - If his answer conflicts with the current decision, discuss it with the user before changing code.

2. **Add a focused ancestry test before broader experiment work.**
   - Run `MDELogging` with ancestry logging on a small deterministic problem and budget.
   - Assert every DE-phase trial has exactly one candidate parent.
   - Assert that parent is the corresponding target used in trial selection.
   - Separately verify local-search nodes still have exactly one immediate-source parent.
   - Avoid relying only on total parent count if the log mixes initialization, DE, and local-search evaluations.

3. **Expose MDE in the EE experiment pipeline.**
   - Extend `EEAlgorithm` with at least distinct `MDE_RAND_1_BIN` and `MDE_BEST_1_BIN` entries using `MDELogging`.
   - Give each variant an unambiguous file prefix.
   - Decide how to pass `eliteSize`, `localSearchFrequency`, `F`, `CR`, and the local-search implementation.
   - Preserve existing ABC and DE behavior.

4. **Reproduce baselines before adaptive changes.**
   - Rastrigin, `D=10`.
   - Compare DE/rand/1/bin, DE/best/1/bin, MDE/rand/1/bin, and MDE/best/1/bin.
   - Include the 20,000-FE condition.
   - Use identical total FE budgets, seeds/common initial populations, and enough repetitions for uncertainty estimates.
   - First reproduce the deterministic `q`/`k` local-search schedule, including `eliteSize=5`, to investigate the professor's question.

5. **Implement/confirm the 500-FE gate.**
   - Do not trigger MDE local search before FE 500 if that is the professor's intended meaning.
   - Make the gate explicit and configurable rather than embedding a magic number.

6. **Generate the existing E/E series and inspect failure modes.**
   - Run `EEAnalysisRunner` on ancestry CSVs.
   - Plot counts/proportions over evaluations for all six non-initial categories.
   - Confirm whether MDE changes successful exploitation, failed exploration, and unsuccessful exploitation as hypothesized.
   - Match the formatting and aggregation of Figures 50 and 158a only after locating their definitions.

7. **Design adaptive local-search triggers only after baseline reproduction.**
   - Candidate triggers should use information available online.
   - Possible signals to investigate include consecutive unsuccessful same-basin trials, stagnation counters, acceptance rate, population diversity, or a cheap proxy for the paper's E/E measures.
   - Clearly distinguish a cheap online proxy from the expensive post-hoc attraction-basin ground truth.
   - Compare adaptive policies against deterministic `q`/`k` under the same FE budget.

8. **Document results answering the professor's questions.**
   - Explain why five elites can consume too much of a 20,000-FE budget.
   - Quantify finite-difference and Armijo evaluation costs.
   - Report whether MDE accelerates successful exploitation and/or reduces unsuccessful exploitation.
   - Report whether local search can actually rescue failed exploration; local search may instead deepen exploitation in the current basin, so this must be tested rather than assumed.

## Suggested first commands for the next Codex instance

```powershell
Set-Location C:\dev\EARS
git status --short
git log -5 --oneline
git show --stat 5d90f14d
rg -n "MDELogging|EEAlgorithm|EEExperimentRunner|EERunAnalyzer" src/org/um/feri/ears
.\gradlew.bat compileJava
```

If Gradle again attempts a network download and the sandbox blocks it, rerun the same compile command with the required approval rather than changing the build.

