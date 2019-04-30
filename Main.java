import java.io.IOException;


public class Main {

	public static void main(String[] args) throws IOException 
	{
		
		//Classify Digits
		NaiveDigits digit_classification = new NaiveDigits();
	
		digit_classification.update_likelihood();
		
		digit_classification.get_new_image();
		
		
		
		//Classify Faces
		NaiveFaces face_classification = new NaiveFaces();
		
		face_classification.update_likelihood();
		
		face_classification.get_new_face();
		

	
	}
}