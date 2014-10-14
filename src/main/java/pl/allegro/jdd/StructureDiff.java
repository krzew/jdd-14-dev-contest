package pl.allegro.jdd;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.PropertyChange;

import pl.allegro.jdd.Changes;
import pl.allegro.jdd.Employee;

public class StructureDiff {

	
	private static final String SALARY_PROPERTY_NAME = "salary";
	
    public Changes calculate(Employee oldCTO, Employee newCTO){
        checkNotNull(oldCTO);
        checkNotNull(newCTO);
        Javers javers = JaversBuilder.javers().build();
        
        Diff javersDiff = javers.compare(oldCTO, newCTO);

        List<Employee> fired = getEmployeesFromChanges(javersDiff.getChanges((change)->(change instanceof ObjectRemoved)));
        List<Employee> hired = getEmployeesFromChanges(javersDiff.getChanges((change)->(change instanceof NewObject)));
        List<Employee> changed = getEmployeesFromChanges( javersDiff.getChanges((change)->(change instanceof PropertyChange && ((PropertyChange)change).getProperty().getName().equals(SALARY_PROPERTY_NAME))));
        
        return new Changes(fired,hired,changed);
    }
    
    private List<Employee> getEmployeesFromChanges(List<Change> changesList){
    	if(null == changesList || changesList.isEmpty()){
    		return Collections.emptyList();
    	}
    	return changesList.stream().map(change->{return (Employee)(change.getAffectedCdo());}).collect(Collectors.toList());
    }
    
	
}
