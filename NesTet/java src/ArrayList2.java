package Bennett.Bernardoni;

import java.util.ArrayList;

public class ArrayList2<E> extends ArrayList<E>{
	public E last(){
		int size = size();
		if(size != 0){
			return get(size-1);
		} else {
			return null;
		}
	}
	
	public E removeLast(){
		int size = size();
		if(size != 0){
			return remove(size-1);
		} else {
			return null;
		}
	}
}
