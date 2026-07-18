package org.um.feri.ears.experiment.ee;

import java.util.ArrayList;
import java.util.List;

public final class EELogNode {

    private final long id;
    private final double fitness;
    private final double[] variables;
    private final List<Long> parentIds;
    private final List<EELogNode> candidateParents = new ArrayList<>();
    private EELogNode parent;
    private Attractor attractor;
    private ExplorationType type = ExplorationType.INITIAL;

    public EELogNode(long id, double fitness, double[] variables, List<Long> parentIds) {
        this.id = id;
        this.fitness = fitness;
        this.variables = variables;
        this.parentIds = new ArrayList<>(parentIds);
    }

    public long getId() { return id; }
    public double getFitness() { return fitness; }
    public double[] getVariables() { return variables; }
    public List<Long> getParentIds() { return parentIds; }
    public List<EELogNode> getCandidateParents() { return candidateParents; }
    public void addCandidateParent(EELogNode parent) { candidateParents.add(parent); }
    public EELogNode getParent() { return parent; }
    public void setParent(EELogNode parent) { this.parent = parent; }
    public Attractor getAttractor() { return attractor; }
    public void setAttractor(Attractor attractor) { this.attractor = attractor; }
    public ExplorationType getType() { return type; }
    public void setType(ExplorationType type) { this.type = type; }
}
