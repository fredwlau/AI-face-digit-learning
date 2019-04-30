import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;


public class NaiveFaces { 

		int x = 2;
		int y = 70;
		int z = 60;
		//0 for face, 1 for digit
		int class_type = 0;
		
		double total = 451;
		double percent = 0;
		double count = 0;
		double smooth = 1;
		double actual_num = 0.0;
		
		double [] num_faces = new double [x];
		double [][] image = new double[y][z];
		
		double [] freq= new double[x];
		double [][] confusion = new double[x][x];
	    double [][][] likelihoods = new double [x][y][z];
	    
	    //pass through functions
	    public void smooth_likelihood() {
	    	NaiveBayes nb = new NaiveBayes();
	    	nb.smooth_likelihood(2, 70, 60, smooth, likelihoods);
	    }
	    
	    public void update_likelihood() {
	    	NaiveBayes nb = new NaiveBayes();
	    	nb.update_likelihood(freq, num_faces, total, smooth, likelihoods,
	    			x, y, z, class_type);
	    }
	    
	    public void pick_face(int input) {
	    	
	    	NaiveBayes nb = new NaiveBayes();
	    	int best = nb.pick(x, y, z, freq, image, likelihoods);
	    	
            if(best == actual_num){	            	
                percent++;	                    
            }
            int casted_num = (int) actual_num;
            confusion[best][casted_num]++;
            
            count++;
            
            if(input ==-1){
                print_results();
            }
	            
	    }
	    
	    //constructor
	    public NaiveFaces() throws IOException {
	        smooth_likelihood();	                    
	        try {
	            FileReader input_stream = new FileReader("facedata/facedatatrain");
	            FileReader labels = new FileReader("facedata/facedatatrainlabels");	
	            
	            int c;
	            int face = 0;	
	            
	            outer:
	            for(int i = 0; true; i++){
                    if(i == 0){
                        face = labels.read();
                        face = face-'0';
                    }
                    for(int j =0; j < 61; j++){
                        c =  input_stream.read();
                        if(c == -1){
                            num_faces[face]++;
                            break outer;
                        }                               
                        else if(c == 35 ){
                            likelihoods[face][i%70][j]++; 
                        }                                                                                              
                    }
                    
                    if(i%70 == 0 && i != 0){
                        face = labels.read();   

                        if(face == -1){
                            break outer;
                        }
                        if (face-'0' != 1 || face-'0' != 0){        
                            face = labels.read();
                        }
                        face = face-'0';
                        num_faces[face]++;  
                    }
                }
	            input_stream.close();
	            labels.close();
            } 
            catch (FileNotFoundException e) {
                    e.printStackTrace();
            }
	    }
	    
	    public void get_new_face() throws IOException {
       
	        try {
	            FileReader input_number = new FileReader("facedata/facedatatest");
	            FileReader actual_number = new FileReader("facedata/facedatatestlabels");
	            
	            int c;
	            
	            outer:
	            for(int i = 0; true; i++){                 
                    if(i%70 == 0 && i != 0){
                        actual_num =  actual_number.read();
	                    if(actual_num  == -1){	                            
	                    	break outer;
	                    }
	                    if(actual_num  == 10) {
	                        actual_num =  actual_number.read();
	                    }
	                    actual_num = actual_num-'0';
                    }
                    
                    for(int j =0; j < 61; j++){
                        c =  input_number.read();
                        if(c == -1){
                            pick_face(c);
                            break outer;                                                
                        }                                  
                        else if(c == 35){
                            image[i%70][j] = 1;
                        }     
                        else if(c == 32){
                            image[i%70][j]= 0;
                        }
                    } 

                    if(i %70==0 && i !=0) {
                        pick_face(1); 
                    }
                    
	            }	
	            input_number.close();
	            actual_number.close();
            }        
            catch (FileNotFoundException e) {
                    e.printStackTrace();
            }            
	    }
	    
	    public void print_results() {
	    	NaiveBayes nb = new NaiveBayes();
            System.out.println("Face Classification: ");
            System.out.println("Count: " +percent+" / "+count);       
            System.out.println("percent: "+ (double)(percent/count)*100);
            System.out.println();
            System.out.println("Confusion Matrix:");
            nb.print_confusion_matrix(x, confusion);
            output_face();
            output_odds();
	    }
	    
	    public void output_face() {
	    	try {
	    		NaiveBayes nb = new NaiveBayes();
		    	BufferedImage img = nb.likelihood_map(x, y, z, 1, likelihoods);
				File output = new File("(face)likelihood.png");
				ImageIO.write(img, "PNG", output);
				
				img = nb.likelihood_map(x, y, z, 0, likelihoods);
				output = new File("(not_face)likelihood.png");
				ImageIO.write(img,"PNG",output);
	    	}
	    	catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	    
	    public void output_odds() {
	    	BufferedImage img = new BufferedImage(z, y, BufferedImage.TYPE_INT_RGB);
	    	NaiveBayes nb = new NaiveBayes();
	    	
			double [][] odds_ratio = new double[y][z];
			double num, min = 100, max = -100;
			for (int i = 0; i < z; i++) {
				for (int j = 0; j < y; j++) {
					num = Math.log(likelihoods[0][j][i]/likelihoods[1][j][i]);
					if (num < min) {
						min = num;
					}
					if (num > max) {
						max = num;
					}
				}
			}
			
			for (int i = 0; i < z; i++) {
				for (int j = 0; j < y; j++) {
					odds_ratio[j][i] = likelihoods[0][j][i]/likelihoods[1][j][i];
					num = Math.log(odds_ratio[j][i]);
					img.setRGB(i, j, nb.get_color(num, min, max));
				}
			}
			
			File output = new File("(face,not_face)ratio.png");
			try { 
				ImageIO.write(img, "PNG", output); 
				}
			catch (IOException e) {
				e.printStackTrace();
			}
	    }
}
