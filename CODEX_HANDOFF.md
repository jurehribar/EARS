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

---

## Codex chat: priprava 4. poglavja poročila (2026-08-06)

Spodaj je prepis uporabniškega pogovora in vsebinskih odgovorov Codexa. Sistemska navodila, klici orodij in dolgi prevajalski izpisi niso vključeni.

### Uporabnik

V poročilo 2
`/users/jurehribar/My Drive/Faks/Doktorski študij/1. letnik izpiti/INDIVIDUALNO RAZISKOVALNO DELO 1 (55D005)/porocilo_02/porocilo_mde.tex`
bi za 3. poglavjem dodal novo poglavje 4.

V tem poglavju bom prikazal poizkuse nad MDE z različnimi parametri za začetek lokalnega iskanja (`mde.localSearchStartFE`).

Potem naj bodo podpoglavje 4.1, 4.2 4.3 za vsako nastavitev parametra.

Parametri, ki se ne spreminjajo in so isti pri vseh poizkusih so:

```properties
mde.problem=Rastrigin
mde.dimension=10
mde.population=100
mde.evaluations=100000
mde.eliteSize=10
mde.localSearchFrequency=10
mde.F=0.5
mde.CR=0.9
```

Spreminjajoč paramater je:

```properties
mde.localSearchStartFE
```

Vrednosti so: 0, 500, 1000, 2500, 5000, 10000 in 15000.

V direktoriju
`/users/jurehribar/My Drive/Faks/Doktorski študij/1. letnik izpiti/INDIVIDUALNO RAZISKOVALNO DELO 1 (55D005)/porocilo_02/Data`
so vsi potrebni podatki.

Imena poddirektorijev nakazujejo nastavitev parametra:
`LS_start00000`, `LS_start00500`, `LS_start01000`, `LS_start02500`, `LS_start05000`, `LS_start10000`, `LS_start15000`.

Poddirektorij `MDE` pomeni strategijo best, `MDErnd` pa strategijo rand. Grafi se nahajajo v poddirektoriju `MDE/Analysis/series/`, tabele pa v `MDE/Analysis`.

Za vsakega od poizkusov dodaj grafe in tabelo. Za MDE best:

1. Turnirski rating.
2. Prvo ponovitev R0 grafa `...-window-all-types.png`.
3. Prvo ponovitev R0 grafa `...-xpl-xpt.png`.
4. Ustrezno tabelo iz `MDE/Analysis`.

Nato še za MDE rand enake štiri vsebine.

### Codex

Dodano je bilo novo 4. poglavje »Vpliv začetka lokalnega iskanja«:

- 4.1 Eksperimentalne nastavitve,
- 4.2 Strategija MDE/best/1/bin,
- 4.3 Strategija MDE/rand/1/bin,
- vseh sedem vrednosti `localSearchStartFE`,
- za vsak poskus turnirski rating, oba grafa R0 in ustrezna tabela.

Široke tabele so bile prikazane ležeče in prilagojene širini strani. Poročilo je bilo uspešno prevedeno.

### Uporabnik

Oprosti, zmotil sem se pri 3. točki. Graf naj bo:
`MDE-best-1-bin-elite10-freq10-startFE0-F0.5-CR0.9_RastriginD10R0P100F100000-window-xpl-xpt`.
Prosim popravi.

### Codex

Tretji graf je bil pri vseh vrednostih in pri obeh strategijah zamenjan z različico `-window-xpl-xpt.png`. Poročilo je bilo ponovno uspešno prevedeno.

### Uporabnik

Kaj so ugotovitve?

### Codex

Glavne ugotovitve:

- Pri Rastriginovi funkciji je za MDE z mutacijsko strategijo `best` primeren zgodnji začetek lokalnega iskanja. Najboljši povprečni rezultat je bil pri `localSearchStartFE = 1000`: $8,46 \pm 3,96$. Podobno dobra je bila nastavitev 0; pri začetku po 5.000 ali več ovrednotenjih se kakovost poslabša.
- Pri strategiji `rand` je rezultat obraten: odloženo lokalno iskanje je koristno. Najboljši rezultat je bil pri `localSearchStartFE = 10000`: $6,37 \pm 3,36$. Dobri sta tudi nastavitvi 5.000 in 15.000.
- MDE-`rand` je pri vseh začetkih dosegel boljši povprečni rezultat kot MDE-`best`.
- Strategija `best` je že sama močno eksploatacijska. Delež eksploracije ostaja približno 3,5--4,4 odstotka.
- Pri strategiji `rand` se z odlaganjem lokalnega iskanja delež eksploracije poveča s približno 26,4 na 36,4 odstotka.
- MDE-`rand` je pri vseh nastavitvah precej boljši od osnovnega DE-`rand`. MDE-`best` izboljša osnovni DE predvsem pri začetkih 0--2.500.
- Ker je bilo izvedenih samo deset ponovitev in so standardni odkloni veliki, razlik med bližnjimi nastavitvami brez statističnega testa ni mogoče razglasiti za statistično značilne.

### Uporabnik

A lahko vse tabele iz 4. poglavja združiš v eno samo, da bo bolj pregledno? Namesto dolgih imen algoritmov naj bo samo recimo `DE-best-1-bin` ali `MDE-best-1-bin`, `startFE` pa kot stolpec v tabeli, da lahko primerjam vse na enem listu?

### Codex

Vseh 14 posameznih tabel je bilo odstranjenih. Rezultati so bili združeni v eno ležečo tabelo na eni strani, uporabljena so bila kratka imena algoritmov, `startFE` je postal ločen stolpec, stalni parametri niso bili ponovljeni, najboljši skupni rezultat pa je bil poudarjen. Poročilo je bilo ponovno prevedeno.

### Uporabnik

A lahko poravnaš legendo zgoraj, da se ujema? Mogoče so smiselne celo vertikalne linije?

### Codex

Dodana je bila dvonivojska centrirana glava. Stolpci so bili združeni v smiselne skupine in med skupinami so bile dodane tanke navpične ločnice. Tabela je ostala na eni ležeči strani in je bila vizualno preverjena.

### Uporabnik

Lahko pogledaš, zakaj je `DE-rand-1-bin` vedno 100 % v exploraciji? Ni ravno smiselno? Sva kaj pokvarila številke pri združevanju tabele? Ali je napaka kje v algoritmu?

### Codex

Ugotovljeno je bilo, da številke pri združevanju niso bile pokvarjene. `DE-rand-1-bin` ni matematično natanko pri 100 %, temveč med 99,9985 % in 99,9995 %, kar se pri štirih decimalkah zaokroži na 1,0000.

V eni pregledani ponovitvi je bilo:

- 100 začetnih osebkov,
- 99.899 dogodkov, razvrščenih kot eksploracija,
- 1 dogodek, razvrščen kot eksploatacija.

Razlaga:

1. Analiza primerja poskusni vektor s ciljnim osebkom `pold[i]`, proti kateremu poteka selekcija.
2. Pri `DE/rand/1/bin` mutant temelji na naključnem osebku `r1`, ne na ciljnem osebku.
3. Ker je $CR=0,9$, se v povprečju zamenja približno devet od desetih koordinat.
4. Rastrigin je močno multimodalen, zato tako različna vektorja skoraj vedno končata v različnih bazenih privlačnosti, kar analiza označi kot eksploracijo.

Ni bilo najdenih znakov napake v implementaciji DE. Opozorjeno pa je bilo, da začetnih 100 osebkov prispeva približno 0,1 odstotne točke k eksploraciji in da MDE v skupni statistiki združuje DE-korake ter notranje korake lokalnega iskanja. Predlagan je bil ločen prikaz XPL/XPT za DE-fazo, lokalno fazo in celoto.

### Uporabnik

Sva rekla samo še, da pri DE kolona `startFE` ne bo parameter, ampak nek znak `--` ali `N/A`.

### Codex

Stolpec `startFE` je bil popravljen tako, da ima številčno vrednost samo pri MDE, pri vseh vrsticah DE pa oznako `--`. Posodobljen je bil tudi opis tabele in preverjen končni izris.

### Uporabnik

Kaj pa je razvidno iz tabele?

### Codex

Iz tabele je bilo povzeto:

- Najboljši skupni rezultat ima `MDE-rand-1-bin` pri `startFE = 10000`: $6,36773 \pm 3,35609$.
- Pri strategiji `best` je najboljši zgodnejši začetek, `startFE = 1000`: $8,45715 \pm 3,95905$.
- Pri `rand` se rezultat z odlaganjem lokalnega iskanja večinoma izboljšuje do vrednosti 10.000.
- `MDE-best-1-bin` ima približno 3,5--4,4 % eksploracije, `MDE-rand-1-bin` pa približno 26--36 %.
- Pri `MDE-rand` večino raziskovalnih dogodkov predstavlja uspešna zavrnitev (SR), približno 70 %, približno 26 % je FE, približno 2 % pa SE.
- Pri obeh MDE-strategijah je večina eksploatacijskih dogodkov neuspešnih (UX).
- Razlik med ponovljenimi vrsticami osnovnega DE se ne sme razlagati kot vpliv `startFE`, saj parameter na DE ne vpliva.
- Zaradi desetih ponovitev in velikih standardnih odklonov bližnje nastavitve brez statističnega testa niso dokazano različne.

### Uporabnik

Dodajva 4.4 z ugotovitvami, tudi tiste, ki si jih prej napisal.

### Codex

Dodano je bilo podpoglavje 4.4 »Ugotovitve«, ki je vključevalo optimalni nastavitvi, primerjavo MDE z DE, vpliv `startFE`, razmerja XPL/XPT, vrste premikov, razlago skoraj 100-odstotne eksploracije pri DE-rand ter omejitve analize. Poročilo je bilo prevedeno in vizualno preverjeno.

### Uporabnik

Lahko ugotovitve prosim skrajšaš in napišeš kot bullete, saj ta tekst še ni za objavo ali profesorja.

### Codex

Ugotovitve v 4.4 so bile skrajšane na osem preglednih alinej. Ohranili so se bistveni rezultati, razlaga eksploracije in glavne omejitve.

### Uporabnik

Dajva jih kar tukaj pisat brez sprememb LaTeXa. Dodaj še kakšno ugotovitev glede tipa SE, FE, DE, SR, kako je s tem pri DE proti MDE in različnih `startFE`.

### Codex

Predlagane so bile naslednje dodatne ugotovitve:

- Pri strategiji `best` so deleži posameznih tipov eksploracije pri DE in MDE podobni. SE predstavlja približno 15--19 %, FE približno 30--35 %, zavajajoča eksploracija (DE) približno 5--7 %, SR pa približno 43--47 % eksploracijskih dogodkov.
- Pri `MDE-best-1-bin` sprememba `startFE` ne povzroči jasnega trenda v deležih SE, FE, DE in SR.
- Pri strategiji `rand` ima MDE znotraj eksploracijskih dogodkov večji delež SE kot osnovni DE: približno 1,7--2,1 % proti približno 0,7--0,8 %.
- `MDE-rand-1-bin` ima približno 1 % zavajajoče eksploracije, osnovni DE pa približno 0,4 %.
- Pri `rand` predstavlja SR približno 70--71 % eksploracijskih dogodkov pri MDE in približno 71--72 % pri osnovnem DE.
- Z večanjem `startFE` se pri `MDE-rand` poveča skupni delež raziskovanja, sestava SE/FE/DE/SR pa ostaja stabilna.
- Relativnega deleža SE ni dovolj primerjati brez skupnega deleža XPL.
- Pri `MDE-best` je SX približno 5--8 %, UX pa 92--95 %.
- Pri `MDE-rand` je SX približno 3--5 % in z večanjem `startFE` večinoma pada, UX pa raste.
- SX/UX pri `DE-rand` nista zanesljiva za interpretacijo, ker je eksploatacijskih dogodkov skoraj nič.
- Stolpec `DE` v skupini vrst premikov pomeni »deceptive exploration«, ne algoritma diferencialne evolucije.

### Uporabnik

Samo tele bullete mi daj v dokument. Dobesedno. Ostale daj stran za ugotovitve:

- MDE-best-1-bin ima samo približno 3,5–4,4 % eksploracije.
- MDE-rand-1-bin ima približno 26–36 % eksploracije.
- Delež eksploracije pri MDE-rand narašča z odlaganjem lokalnega iskanja.
- Pri Rastriginovi funkciji je za MDE z mutacijsko strategijo best primeren zgodnji začetek lokalnega iskanja. Najboljši povprečni rezultat je bil dosežen pri localSearchStartFE = 1000. Pri začetku po 5.000 ali več ovrednotenjih se kakovost občutno poslabša.
- Pri strategiji rand je rezultat obraten: odloženo lokalno iskanje je koristno. Najboljši povprečni rezultat je bil dosežen pri localSearchStartFE = 10000:
- MDE-rand je pri vseh preizkušenih začetkih dosegel boljši povprečni rezultat kot MDE-best. Največja razlika je vidna pri poznejšem začetku lokalnega iskanja.
- Pri strategiji rand se z odlaganjem lokalnega iskanja delež eksploracije poveča s približno 26,4 % na 36,4 %. Algoritem tako najprej učinkoviteje razišče prostor, nato pa lokalno iskanje izboljša najobetavnejše rešitve.
- Pri strategiji rand ima MDE znotraj eksploracijskih dogodkov večji delež uspešne eksploracije kot osnovni DE. Pri MDE je SE približno 1,7–2,1 %, pri osnovnem DE pa samo približno 0,7–0,8 %.
- MDE-rand-1-bin ima tudi nekoliko več zavajajoče eksploracije: približno 1 % v primerjavi s približno 0,4 % pri osnovnem DE. To pomeni, da lokalno iskanje poveča delež prehodov, ki trenutno izboljšajo rešitev, vendar vodijo v slabši bazen privlačnosti.
- Pri MDE-best-1-bin je uspešnih samo približno 5–8 % eksploatacijskih premikov (SX), preostalih 92–95 % pa predstavlja neuspešno eksploatacijo (UX). Lokalno iskanje torej izvede veliko poskusov v istem bazenu, vendar le manjši delež neposredno izboljša rešitev.
- Pri MDE-rand-1-bin je delež uspešne eksploatacije še manjši, približno 3–5 %. Z večanjem startFE se SX večinoma zmanjšuje, UX pa povečuje. Kljub temu se končni rezultat izboljšuje, kar kaže, da je pomembnejša kakovost območja, v katerem se lokalno iskanje začne, in ne samo delež uspešnih lokalnih korakov.

### Codex

V 4.4 so bile odstranjene vse prejšnje alineje in nadomeščene samo z uporabnikovimi enajstimi alinejami. Zaradi LaTeXa so bili znaki `%` ubežani, razponi pa zapisani z `--`; prikazana vsebina je ostala enaka. Poročilo je bilo uspešno prevedeno in vizualno preverjeno.

### Uporabnik

Dodaj tale cel chat v `CODEX_HANDOFF.md`.

### Trenutno stanje poročila po pogovoru

- Izvor: `/Users/jurehribar/My Drive/Faks/Doktorski študij/1. letnik izpiti/INDIVIDUALNO RAZISKOVALNO DELO 1 (55D005)/porocilo_02/porocilo_mde.tex`
- PDF: `/Users/jurehribar/My Drive/Faks/Doktorski študij/1. letnik izpiti/INDIVIDUALNO RAZISKOVALNO DELO 1 (55D005)/porocilo_02/porocilo_mde.pdf`
- Delovna kopija v repozitoriju: `porocilo_mde.working.tex`
- Poglavje 4 vsebuje 4.1 nastavitve, 4.2 MDE-best, 4.3 MDE-rand in 4.4 ugotovitve.
- Vključeni so rating, `window-all-types` in `window-xpl-xpt` grafi za vseh sedem vrednosti ter obe strategiji.
- Vse tabele so združene v eno ležečo tabelo.
- Vrstice DE imajo v stolpcu `startFE` oznako `--`.
- Zadnja uspešna kompilacija je ustvarila 38-stranski PDF.

---

## Codex work: Ackley comparison and report Chapter 5 (2026-08-07)

### Scope completed today

Today’s work was limited to analysis of the existing Ackley experiment data and editing the LaTeX report. No Java source code or experiment implementation was changed.

The analyzed fixed configuration was:

```properties
mde.problem=Ackley
mde.dimension=10
mde.population=50
mde.evaluations=25000
mde.eliteSize=3
mde.localSearchFrequency=5
mde.localSearchStartFE=5000
mde.F=0.5
mde.CR=0.9
```

Algorithms compared:

- `MDE-rand-1-bin-elite3-freq5-startFE5000-F0.5-CR0.9`
- `DE-rand-1-binOldNew`

There were 10 independent runs per algorithm.

### Ackley local-search-start comparison

The new `localSearchStartFE=2500` run was compared with starts 1000, 5000, 10000, and 15000 for the same Ackley/MDE configuration.

Median FEs needed to reach selected targets were:

| startFE | $10^{-7}$ | $10^{-10}$ | $10^{-12}$ | $10^{-14}$ |
|---:|---:|---:|---:|---:|
| 1000 | 7885.5 | 9141 | 10208.5 | 11338 |
| 2500 | 4194 | 5940 | 7082 | 8238 |
| 5000 | 6248.5 | 7789 | 9160 | 10531 |
| 10000 | 11198 | 12851 | 14224 | 15620 |
| 15000 | 15266 | 16768.5 | 18139.5 | 19512.5 |

Interpretation:

- Start 2500 had the best median convergence speed.
- It was less reliable than start 5000. For target $10^{-7}$ its run range was 3665–13462 FEs, compared with 5865–6374 for start 5000.
- A random start-2500 run beat a random start-5000 run about 70% of the time, but the exact rank comparison with only 10 runs per group gave approximately $p=0.143$, so superiority over start 5000 was not conclusive.
- Start 5000 remained the most consistent setting.
- Recommended future check: starts 2500, 3750, and 5000 with at least 30 runs and matched seeds if possible.

### DE versus MDE conclusion for `Ackley_E03_F05_P050_LSS05000`

The algorithms behaved comparably before the MDE local-search gate:

- FE 1000: MDE median 13.8209, DE median 13.8398.
- FE 2500: MDE median 5.4150, DE median 5.6577.
- FE 5000: MDE median 0.8312, DE median 0.7666.

Immediately after local search became active, MDE improved sharply:

- FE 5100: MDE median 0.0068248, DE median 0.6473.
- FE 6000: MDE median $2.7287\times10^{-7}$, DE median 0.1700.
- FE 10000: MDE median $9.4591\times10^{-14}$, DE median 0.0023282.
- FE 25000: MDE median $3.9968\times10^{-15}$, DE median $9.1195\times10^{-10}$.

All 10 MDE runs reached $10^{-14}$, with median hitting time 10531 FEs. No DE run reached $10^{-10}$ within 25000 FEs. All MDE final results were better than all DE final results.

Exploration/exploitation summaries:

- MDE XPL/XPT: 0.186856 / 0.813144.
- DE XPL/XPT: 0.182688 / 0.817312.
- MDE SX/UX: 0.079970 / 0.920030.
- DE SX/UX: 0.274925 / 0.725075.
- SE, FE, deceptive exploration, and successful rejection proportions were very similar between algorithms.

Main interpretation: MDE did not win by spending a larger fraction of evaluations in exploitation. Instead, the local-search evaluations immediately after FE 5000 produced much larger objective improvements. The low later SX ratio for MDE is consistent with a population already extremely close to the optimum, where further improvements are rare.

This conclusion is limited to Ackley, $D=10$, population 50, the listed MDE settings, and 10 runs.

### New Chapter 5 in the LaTeX report

Current Windows report source:

```text
C:\Users\JureHribar\My Drive\Faks\Doktorski študij\1. letnik izpiti\IRD 1\porocilo_02\porocilo_mde.tex
```

Copied experiment data:

```text
C:\Users\JureHribar\My Drive\Faks\Doktorski študij\1. letnik izpiti\IRD 1\porocilo_02\Data\Ackley_E03_F05_P050_LSS05000
```

A new section was inserted immediately before the previous `\section{Razprava}`, so it is Chapter/Section 5:

```latex
\section{Primerjava MDE in DE na Ackleyjevi funkciji}
```

Only this new chapter was added. It contains:

1. A table of experiment settings.
2. MDE R0 `window-all-types` and `window-xpl-xpt` figures.
3. DE R0 `window-all-types` and `window-xpl-xpt` figures.
4. Table 5 with aggregate E/E metrics and final fitness.
5. Table 6 with convergence values at selected FE checkpoints.
6. Seven short findings.

All image paths are relative to the report and start with:

```text
Data/Ackley_E03_F05_P050_LSS05000/...
```

This was done intentionally so the document continues to work when moved back to macOS. All four referenced Ackley image files were verified to exist.

There is no tournament-rating image in the copied Ackley folder, so no rating figure was added.

### Table 5 calculation

Table 5 is `tab:ackley-ee-rezultati`. It does not contain medians. For every metric, EARS first calculates one value per run, then reports the arithmetic mean and sample standard deviation over 10 runs:

```text
mean = sum(x_r) / n
sample stdev = sqrt(sum((x_r - mean)^2) / (n - 1))
```

The implementation is in:

- `src/org/um/feri/ears/experiment/ee/MetricAccumulator.java`
- `src/org/um/feri/ears/experiment/ee/EEMetrics.java`

SE/FE/DE/SR are normalized within exploration events, SX/UX within exploitation events, XPL/XPT over overall search behavior, and Best is the best fitness in each run before aggregation.

### Final form of Table 6

Table 6 is `tab:ackley-konvergenca`.

It initially contained medians and exact rank-test p-values. At the user's request it was changed to arithmetic mean ± sample standard deviation, and the p-value column was then removed completely. Chapter 5 now contains no p-values or p-value claims.

For each run $r$ and checkpoint $t$, the input value is the best objective observed up to that checkpoint:

```text
b_r(t) = min(f_r,1, ..., f_r,t)
```

The displayed mean and sample standard deviation are calculated across the 10 values $b_r(t)$.

Current Table 6 values:

| FEs | MDE mean ± sample SD | DE mean ± sample SD |
|---:|---:|---:|
| 1000 | 13.7788 ± 2.0212 | 13.6879 ± 1.0885 |
| 2500 | 5.4950 ± 1.0878 | 5.5501 ± 0.6156 |
| 5000 | 0.9136 ± 0.4795 | 0.7506 ± 0.1859 |
| 5100 | 0.01093 ± 0.01047 | 0.6764 ± 0.1984 |
| 6000 | $(5.2149 ± 6.3795)\times10^{-7}$ | 0.2026 ± 0.0945 |
| 10000 | $(1.6778 ± 1.9490)\times10^{-13}$ | $(2.5953 ± 0.8537)\times10^{-3}$ |
| 25000 | $(2.9310 ± 1.7161)\times10^{-15}$ | $(9.5240 ± 4.8370)\times10^{-10}$ |

The two findings that previously referred to medians and p-values were updated to use means and no p-values.

### Temporary Chapter 4 workaround

Compilation exposed an unrelated pre-existing missing-data problem in Chapter 4. The directory below is absent:

```text
Data/LS_start00500/MDE
```

`Data/LS_start00500` contains only `MDErnd` and `rating.png`; every other `LS_start*` directory contains both `MDE` and `MDErnd`. No copy of the missing MDE-best/startFE500 data was found in `C:\dev\TestEARS`, `C:\dev\EARS`, or the searched Google Drive directories.

The following call in `porocilo_mde.tex` was therefore temporarily commented out, exactly as requested:

```latex
% Začasno izključeno: manjka imenik Data/LS_start00500/MDE.
% \LSExperiment{LS_start00500}{500}{best}{MDE}{rating.png}
```

Restore the missing directory and uncomment the call when the data becomes available.

### Verification and remaining limitation

- Four Ackley figure paths were verified to exist.
- The added Chapter 5 contains 4 figures, 3 tables, and 7 findings.
- LaTeX `begin`/`end` environments in the added chapter were counted and balanced.
- No absolute Windows image paths occur in Chapter 5.
- `pdflatex`, `latexmk`, and `tectonic` were not installed or available through `PATH` on this Windows machine, so Codex could not regenerate or visually inspect the final PDF locally.
