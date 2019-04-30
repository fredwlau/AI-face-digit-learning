import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class NaiveDigits {
	
	int x = 10;
	int y = 28;
	int z = 28;
	int class_type = 1;
	
	double actual_num = 0;
	double percent = 0;
	double count = 0;
	double smooth = 1;
	double total = 5000;
	
	double [] numbers = new double [x];
	double [] freq = new double[x]; 
	double [][] image = new double[y][z];
	double [][] confusion = new double [x][x];
	double [][][] likelihoods = new double [x][y][z];
	
	//pass through functions
	public void smooth_likelihood() {
		NaiveBayes nb = new NaiveBayes();
		nb.smooth_likelihood(10, 28, 28, smooth, likelihoods);
	}
	
	public void update_likelihood() {
		NaiveBayes nb = new NaiveBayes();
		nb.update_likelihood(freq, numbers, total, smooth, likelihoods, x, y, z, class_type);
	}
	
	public void pick_class( int input){
		
    	NaiveBayes nb = new NaiveBayes();
    	int best = nb.pick(x, y, z, freq, image, likelihoods);

		if(best == actual_num) {
			percent++;	
		}
		int casted_num = (int) actual_num;
		confusion[best][casted_num]++;
		count++;
		
		if(input ==-1) {
			print_results();
		}
	}
	
	//constructor
	public NaiveDigits() throws IOException {
		smooth_likelihood();
		try {
	        FileReader input_stream = new FileReader("digitdata/trainingimages");
	        Scanner labels = new Scanner(new File("digitdata/traininglabels"));
	        
	        int c;
	        int index = labels.nextInt();
	        
	        outerloop:
	        for(int i = 0; true; i++){	        	
				for(int j =0; j < 29; j++) {
					c =  input_stream.read();
					
					switch (c) {
					case -1:
						numbers[index]++;
						break outerloop;
					case 35:
					case 43:
						likelihoods[index][i%28][j]++;
					}
				}
	
				if(i%28 == 0 && i != 0) {
					numbers[index]++;
	
					if (!labels.hasNextInt())
						break outerloop;
					else
						index = labels.nextInt();
				}
			}	
	        input_stream.close();
	        labels.close();
		}
		catch (FileNotFoundException e){
			e.printStackTrace();
		}
	}
	
	public void get_new_image() throws IOException{
		
		try {
			FileReader input_num = new FileReader("digitdata/testimages");
			Scanner actual_number = new Scanner(new File("digitdata/testlabels"));
	
	        int c;
				
	        outerloop:
	        for(int i = 0; true; i++) {		 
				if(i %28 == 0 && i != 0) {
					if (!actual_number.hasNextInt())
						break outerloop;
					else
						actual_num = actual_number.nextInt();
				}
				
				for(int j =0; j < 29; j++) {
	
					c =  input_num.read();
					
					switch (c) {
					case -1:
						pick_class(c);
						break outerloop;
					case 35:
					case 43:
						image[i%28][j] = 1;
						break;
					case 32:
						image[i%28][j] = 0;
						break;
					}
				}
	
				if(i %28==0 && i !=0)
					pick_class(1);
			}
	        input_num.close();
	        actual_number.close();
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void odds(int c1, int c2) throws IOException {
		NaiveBayes nb = new NaiveBayes();
		BufferedImage img = new BufferedImage(84, 28, BufferedImage.TYPE_INT_RGB);
		BufferedImage img_c1 = nb.likelihood_map(x, y, z, c1, likelihoods);
		BufferedImage img_c2 = nb.likelihood_map(x, y, z, c2, likelihoods);
		
		double [][] odds_ratio = new double[y][z];
		
		double num, min = 100, max = -100;
		for (int i = 0; i < y; i++) {
			for (int j = 0; j < z; j++) {
				num = Math.log(likelihoods[c1][j][i]/likelihoods[c2][j][i]);
				if (num < min) {
					min = num;
				}
				if (num > max) {
					max = num;
				}
			}
		}
		
		for (int i = 0; i < y; i++) {
			for (int j = 0; j < z; j++) {
				odds_ratio[j][i] = likelihoods[c1][j][i]/likelihoods[c2][j][i];
				num = Math.log(odds_ratio[j][i]);
				img.setRGB(i, j, nb.get_color(num, min, max));
				img.setRGB(i+y, j, img_c1.getRGB(i, j));
				img.setRGB(i+(y*2), j, img_c2.getRGB(i, j));
			}
		}

		
		File output = new File("(" + c1 + "," + c2 + ")ratio.png");
		try {
			ImageIO.write(img, "PNG", output);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void output_odds() {
		try {
			odds(8,3);
			odds(9,7);
			odds(5,3);
			odds(9,4);
		}
		catch ( IOException e) {}
	}
	
	public void output_digits() {
		try {
			NaiveBayes nb = new NaiveBayes();
			for (int i = 0; i < 10; i++) {
				BufferedImage img = nb.likelihood_map(x, y, z, i, likelihoods);
				File output = new File("(" + i + ")likelihood.png");
				ImageIO.write(img, "PNG", output);
			}
		} catch ( IOException e ) {}
	}
	
	public void print_results() {
		NaiveBayes nb = new NaiveBayes();
		System.out.println("--------------------NAIVE BAYES RESULTS--------------------");
		System.out.println("Digit Classification: ");
		System.out.println("Count: " +percent+" / "+count);
		System.out.println("Percent: "+ (float)(percent/count)*100);
		System.out.println("Confusion Matrix:");
		nb.print_confusion_matrix(x, confusion);
		output_digits();
		output_odds();
	}
}
