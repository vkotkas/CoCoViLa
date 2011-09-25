/**
 * 
 */
package ee.ioc.cs.vsle.table;

import java.util.*;

/**
 * @author pavelg
 *
 */
public class TableInferenceEngine {

    /**
     * Verifies conditions and returns corresponding index
     * 
     * @param ids
     * @param rules
     * @param args
     * @return
     */
    public static int checkRules( TableFieldList<InputTableField> inputFields, List<Integer> ids, List<Rule> rules, Object[] args ) {
        outer: for ( Integer id : ids ) {
            
            //if a row does not contain any rule entries it is valid by default
            boolean isOK = true;
            
            for ( Rule rule : rules ) {
                if ( rule.getEntries().contains( id ) ) {
                    isOK &= rule.verifyCondition( args[ inputFields.indexOf( rule.getField() )] );
                }
                
                if( !isOK ) 
                    continue outer;
            }
            
            return id;
        }
        
        throw new TableException( "No valid rules for current input: " + Arrays.toString( args ) );
    }
    
    /**
     * If a constraint is violated, TableInputConstraintViolationException is thrown
     * 
     * @param inputFields
     * @param args
     */
    public static void verifyInputs( TableFieldList<InputTableField> inputFields, Object[] args ) {
        for( int i = 0; i < inputFields.size(); i++ ) {
            inputFields.get( i ).verifyConstraints( args[i] );
        }
    }
    
    /**
     * @param table
     * @return
     */
    public static InputTableField getFirstTableInput( Table table ) {
        
        List<Integer> rowIds = table.getOrderedRowIds();
        //first, try horisontal rules
        if(!rowIds.isEmpty()) {
            int id = rowIds.get( 0 );
            for ( Rule rule : table.getHRules() ) {
                if ( rule.getEntries().contains( id ) ) {
                    return rule.getField();
                }
            }
        }
        
        List<Integer> colIds = table.getOrderedColumnIds();
        //if nothing there, try vertical ones
        if(!colIds.isEmpty()) {
            int id = colIds.get( 0 );
            for ( Rule rule : table.getVRules() ) {
                if ( rule.getEntries().contains( id ) ) {
                    return rule.getField();
                }
            }
        }
        //otherwise no inputs are required in order to get a value from a table
        return null;
    }

    static class ProductionRule {
        int id;
        List<Rule> rules = new ArrayList<Rule>();
        TableFieldList<InputTableField> inputFields = new TableFieldList<InputTableField>();

        @Override
        public String toString() {
            return "ProductionRule [id=" + id + ", rules=" + rules
                    + ", inputFields=" + inputFields + "]";
        }
        
        
    }
    
    public static class InputFieldAndSelectedId {
        
        private InputTableField input;
        private Integer selectedId;
        
        private InputFieldAndSelectedId() {}
        
        /**
         * @param input
         * @param selectedId
         */
        InputFieldAndSelectedId( InputTableField input,
                Integer selectedId ) {
            this();
            
            this.input = input;
            this.selectedId = selectedId;
        }
        /**
         * @return the input
         */
        public InputTableField getInput() {
            return input;
        }
        /**
         * @return the selectedId
         */
        public Integer getSelectedId() {
            return selectedId;
        }
    }
    
    /**
     * @param input
     * @return
     */
    public static InputFieldAndSelectedId getNextInputAndRelevantIds( List<Rule> rules, InputTableField input, 
            Map<InputTableField, Object> inputsToValues, List<Integer> allIds, List<Integer> outIds ) {
        
        assert inputsToValues.containsKey( input );
        
        //the ids that hold for current input
        Map<Integer, ProductionRule> productionRules = new LinkedHashMap<Integer, ProductionRule>();
        
        for ( Rule rule : rules ) {
            System.out.println("Rule: " + rule );
            InputTableField ruleInput = rule.getField();
            
            //if null, the rule does not match the input
            boolean matchesInput = ruleInput.equals( input );
            Boolean holds = matchesInput ? rule.verifyCondition( inputsToValues.get( input ) ) : null;
            
            for( Integer id : rule.getEntries() ) {
                System.out.println("id: " + id + " contains in all available ids: " + allIds.contains( id ) );
                if( !allIds.contains( id ) ) continue;
                //
                ProductionRule pr;
                if( ( pr = productionRules.get( id ) ) == null ) {
                    pr = new ProductionRule();
                    pr.id = id;
                    productionRules.put( id, pr );
                }
                pr.rules.add( rule );
                if( !matchesInput && !pr.inputFields.contains( ruleInput ) 
                        && !inputsToValues.containsKey( ruleInput ) )
                    //store only unknown rule's inputs here
                    pr.inputFields.add( ruleInput );
                //
                if( holds != null ) {//this id is a candidate for removal
                    boolean contains;
                    if( ( contains = outIds.contains( id ) ) && !holds ) {
                        outIds.remove( id );
                    } else if( !contains && holds ) {
                        outIds.add( id );
                    }
                } else {//does not contain current input, so could be useful later
                    outIds.add( id );
                }
            }
        }
        
        System.out.println(productionRules.size() + " - All production rules: " + productionRules );
        System.out.println("All available ids: " + allIds );
        InputFieldAndSelectedId result = null;//the result will be created only once but the iteration will continue until all ids are examined
        for ( Integer id : allIds ) {//we need to iterate all ids because some of rows/cols may contain no rules!
            ProductionRule prodr = productionRules.get( id );
            //null means empty row that should be considered
            //non null and not empty should be among out_ids
            if( prodr == null || ( prodr.inputFields.isEmpty() && outIds.contains( prodr.id ) ) ) {
                if( !outIds.contains( id ) )//TODO
                    outIds.add( id );//this is required in order to keep empty rows/cols for the future use
                //if no other inputs are needed, do not proceed and return null
                System.out.println("no other inputs are needed, do not proceed and return null");
                if( result == null )
                    result = new InputFieldAndSelectedId( null, id );
                continue;
            } else if( !outIds.contains( prodr.id ) ) {
                continue;
            }
            System.out.println( "returning input " + prodr.inputFields.get( 0 ) );
            if( result == null )
                result = new InputFieldAndSelectedId( prodr.inputFields.get( 0 ), null );
        }
        System.out.println("Out ids: " + outIds );
        
        if( result == null && outIds.size() != 0 ) {//there is still some hope left!
            System.out.println("there is still some hope left! " + result );
            int id = outIds.get( 0 );
            ProductionRule prodr = productionRules.get( id );
            if( prodr != null ) {
                if( !prodr.inputFields.isEmpty() )
                    result = new InputFieldAndSelectedId( prodr.inputFields.get( 0 ), null );
            } else {
                result = new InputFieldAndSelectedId( null, id );
            }
        }
        System.out.println( "returning " + result );
        return result == null ? new InputFieldAndSelectedId() : result;
    }
    
}
