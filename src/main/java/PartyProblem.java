import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;

import org.chocosolver.solver.variables.VariableFactory;

/**
 *
 * @author Joris
 */
public class PartyProblem {
    
    Solver solver;
    IntVar algemeneTevredenheid;
    IntVar[][] tapToekenningen;
    IntVar[][] bonnekesToekenningen;
    final static String[] MEDEWERKERS = {"Robbe ", "Lisa  ", "Seppe ", "Dorien", "Wesley"};

    public PartyProblem() {
        
        solver = new Solver("Fuifprobleem");
        
        // Initialisatie in te plannen variabelen
        // 5 rijen (medewerkers) en 4 kolommen (shifts)
        tapToekenningen = VariableFactory.boundedMatrix("tapToekenningen", 5, 4, 0, 1, solver);
        bonnekesToekenningen = VariableFactory.boundedMatrix("bonnekesToekenningen", 5, 4, 0, 1, solver);
        
        // Medewerker kan slechts op 1 plaats tegelijk zijn in shift
        for (int m=0; m < 5; m ++) {
            for (int s=0; s < 4; s ++) {
                Constraint nietTegelijk = IntConstraintFactory.arithm(tapToekenningen[m][s], "+", bonnekesToekenningen[m][s], "<", 2);
                solver.post(nietTegelijk);
            }
        }
        
        // Gewenste bezetting per shift
        int[] tapBezetting = {1, 2, 3, 2};
        int[] bonBezetting = {1, 2, 2, 1};
        for (int s=0; s < 4; s ++) {
             IntVar[] tapShift = new IntVar[5];
             IntVar[] bonShift = new IntVar[5];
             for (int m=0; m < 5; m ++) {
                 tapShift[m] = tapToekenningen[m][s];
                 bonShift[m] = bonnekesToekenningen[m][s];
             }
             Constraint tapShiftC = IntConstraintFactory.sum(tapShift, VariableFactory.fixed(tapBezetting[s], solver));
             solver.post(tapShiftC);
             Constraint bonShiftC = IntConstraintFactory.sum(bonShift, VariableFactory.fixed(bonBezetting[s], solver));
             solver.post(bonShiftC);
        }
        
        // maximaal 3 shiften per medewerker
        /*
        for (int m=0; m < 5; m ++) {
            IntVar[] alMijnVensters = {tapToekenningen[m][0], tapToekenningen[m][1], tapToekenningen[m][2], tapToekenningen[m][3],
                bonnekesToekenningen[m][0], bonnekesToekenningen[m][1], bonnekesToekenningen[m][2], bonnekesToekenningen[m][3],
            };
            Constraint geenOverbelasting = IntConstraintFactory.sum(alMijnVensters, "<", VariableFactory.fixed(4, solver));
            
            
            //Constraint geenOverbelasting = IntConstraintFactory.sum(tapToekenningen[m], "<", VariableFactory.fixed(3, solver));
            solver.post(geenOverbelasting);
        }
        */
        
        // Toevoeging variabele algemene tevredenheid
        algemeneTevredenheid = VariableFactory.bounded("algemeneTevredenheid", 0, 999, solver);
        IntVar[] alleVars = new IntVar[5*4*2];
        for (int m=0; m < 5; m ++) {
            for (int s=0; s < 4; s ++) {
                alleVars[m*4*2+s] = tapToekenningen[m][s];
            }
            for (int s=0; s < 4; s ++) {
                alleVars[m*4*2+4+s] = bonnekesToekenningen[m][s];
            }
        }
        solver.post(IntConstraintFactory.scalar(alleVars, new int[]{
            2,2,10,10, //Robbe
            0,0,0,0, 
            0,0,0,0, //Lisa
            10,8,4,2,
            10,0,10,0, //Seppe
            0,7,0,0,
            5,5,0,0,  //Dorien
            10,8,0,0,
            0,10,10,10, //Wesley
            6,0,0,0
        }, algemeneTevredenheid));
    }
    
    public void zoekOptimaleOplossing() {
        solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, algemeneTevredenheid);
        printOplossing();
        
    }
    
    public void zoekOptimaleOplossingInNEerste(int n) {
        solver.findSolution();
        int maxScore = algemeneTevredenheid.getValue();
        printOplossing();
        for (int i=1; i < n ; i++) {
            if (solver.nextSolution()) {
                if (algemeneTevredenheid.getValue() > maxScore) {
                    maxScore = algemeneTevredenheid.getValue();
                    printOplossing();
                }
            } else break;
        }
        System.out.println(maxScore);
        
    }
    
    public void printOplossing() {
        for (int m=0; m < 5; m ++) {
            System.out.print(MEDEWERKERS[m] + " ");
            for (int s=0; s < 4; s ++) {
                System.out.print( (tapToekenningen[m][s].getValue() == 1 ? 'T' : (bonnekesToekenningen[m][s].getValue() == 1 ? 'B' : '-')) + " " );
            }
            System.out.println();
        }
        System.out.println("Score: " + algemeneTevredenheid.getValue());
    }
    
    public static void main(String[] args) {
        
        PartyProblem fuif = new PartyProblem();
        
        fuif.zoekOptimaleOplossing();
        
                
        
    }

    
}
