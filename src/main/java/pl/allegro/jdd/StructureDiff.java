package pl.allegro.jdd;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;

import pl.allegro.jdd.Changes;
import pl.allegro.jdd.Employee;

public class StructureDiff {

	
	private static final String SALARY_PROPERTY_NAME = "salary";
	
    public Changes calculate(Employee oldCTO, Employee newCTO){
        checkNotNull(oldCTO);
        checkNotNull(newCTO);
        Javers javers = JaversBuilder.javers().build();
        
        Diff javersDiff = javers.compare(oldCTO, newCTO);

        List<Employee> fired = javersDiff.getObjectsByChangeType(ObjectRemoved.class);
        List<Employee> hired = javersDiff.getObjectsByChangeType(NewObject.class);
        List<Employee> changed = javersDiff.getObjectsWithChangedProperty(SALARY_PROPERTY_NAME);
        
        return new Changes(fired,hired,changed);
    }
    
	
}
