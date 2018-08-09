import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;


public class Main {

    public static void main(String[] args) {
        final double BI = 15.36 * (1 << 5);
        final Task[] T = {
                new Task(32, 0),
                new Task(16, 1),
                new Task(16, 2),
                new Task(16, 3),
                new Task(0, 4),
                new Task(16, 5),

                new Task(0, 2),
                new Task(0, 0),
                new Task(0, 1),
                new Task(0, 3),
                new Task(0, 5),

                new Task(0, 1),
                new Task(0, 0),
                new Task(0, 2)
        };
        final int n = T.length;

        final int[][] w = new int[n][n];
        final int[][] v = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                w[i][j] = -Integer.MAX_VALUE;
                v[i][j] = -Integer.MAX_VALUE;
            }
        }
        w[10][8] = 16;
        w[8][7] = 16;
        w[7][6] = 32;
        w[6][9] = -50;
        w[9][7] = 16;
        w[6][10] = -633;

        w[12][11] = 32;
        w[13][12] = 16;
        w[11][13] = -775;

        v[10][5] = 0;
        v[5][10] = 0;

        v[8][1] = 0;
        v[1][8] = 0;
        v[1][11] = 0;
        v[11][1] = 0;

        v[7][0] = 0;
        v[0][7] = 0;
        v[0][12] = 0;
        v[12][0] = 0;

        v[6][2] = 0;
        v[2][6] = 0;
        v[2][13] = 0;
        v[13][2] = 0;

        v[9][3] = 0;
        v[3][9] = 0;


        final boolean[][] c = {
                {false, true, true, true, true, true},
                {true, false, true, true, true, true},
                {true, true, false, true, true, true},
                {true, true, true, false, false, false},
                {true, true, true, false, false, true},
                {true, true, true, false, true, false}
        };

        final boolean[][] M = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                M[i][j] = (T[i].getProcessTime() > 0) && (T[j].getProcessTime() > 0)
                        && c[T[i].getCluster()][T[j].getCluster()];
            }
        }

        try {
            IloCplex cplex = new IloCplex();

            int[] lb = new int[n];
            int[] ub = new int[n];
            String[] sname = new String[n];
            String[] qname = new String[n];
            for (int i = 0; i < n; i++) {
                ub[i] = (int) BI - T[i].getProcessTime();
                sname[i] = String.format("s%d", i);
                qname[i] = String.format("q%d", i);
            }

            IloIntVar[] s = cplex.intVarArray(n, lb, ub, sname);
            IloIntVar[] q = cplex.intVarArray(n, 0, Integer.MAX_VALUE, qname);
            IloIntVar[] x = cplex.boolVarArray(n * n);
            IloLinearNumExpr expr = cplex.linearNumExpr();
            for (int i = 0; i < n; i++) {
                expr.addTerm(1, s[i]);
                expr.addTerm(BI, q[i]);
            }
            cplex.addMinimize(expr);

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j && w[i][j] != -Integer.MAX_VALUE) {
                        IloLinearNumExpr constraint = cplex.linearNumExpr();
                        constraint.addTerm(1, s[j]);
                        constraint.addTerm(BI, q[j]);
                        constraint.addTerm(-1, s[i]);
                        constraint.addTerm(-BI, q[i]);
                        cplex.addGe(constraint, w[i][j], "(9)");
                    }
                }
            }

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i != j && v[i][j] != -Integer.MAX_VALUE) {
                        IloLinearNumExpr constraint = cplex.linearNumExpr();
                        constraint.addTerm(1, s[j]);
                        constraint.addTerm(-1, s[i]);
                        cplex.addGe(constraint, v[i][j], "(10)");
                    }
                }
            }

            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (M[i][j]) {
                        IloLinearNumExpr constraint1 = cplex.linearNumExpr();
                        constraint1.addTerm(1, s[j]);
                        constraint1.addTerm(-1, s[i]);
                        constraint1.addTerm(BI, x[i * n + j]);
                        cplex.addGe(constraint1, T[j].getProcessTime(), "(11)");

                        IloLinearNumExpr constraint2 = cplex.linearNumExpr();
                        constraint2.addTerm(1, s[j]);
                        constraint2.addTerm(-1, s[i]);
                        constraint2.addTerm(BI, x[i * n + j]);
                        cplex.addLe(constraint2, BI - T[i].getProcessTime(), "(12)");

                    }
                }
            }

            cplex.exportModel("simulation.lp");


            if (cplex.solve()) {
                double objval = cplex.getObjValue();
                System.out.println(objval);

                double[] sval = cplex.getValues(s);
                for (int i = 0; i < n; i++) {
                    System.out.printf("s[%d]: %g\n", i, sval[i]);
                }

                double[] qval = cplex.getValues(q);
                for (int i = 0; i < n; i++) {
                    System.out.printf("q[%d]: %g\n", i, qval[i]);
                }
            } else {
                System.out.printf("Solve failed: %s\n", cplex.getStatus());
            }

        } catch (IloException e) {
            e.printStackTrace();
        }
    }
}
