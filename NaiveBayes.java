import java.awt.image.BufferedImage;

public class NaiveBayes {
	
	public int get_color(double x, double min, double max) {
		double r = 0,g = 0,b = 0;

		double y = x;
		double m = (240)/(min-max);
		double a = (-240*max)/(min-max);

		y = y*(m) + a;

		int i;
		double f, p, q, t;
		double h,s,v;
		h = y;
		s = v = 100;

		h = Math.max(0, Math.min(360, h));
		s = Math.max(0, Math.min(100, s));
		v = Math.max(0, Math.min(100, v));
	 
		s /= 100;
		v /= 100;
	 
		if(s == 0) {
			// Achromatic (grey)
			r = g = b = v;
			r = Math.round(255*r);
			g = Math.round(255*g);
			b = Math.round(255*b);
		}
		else {
			h /= 60; 
			i = (int)Math.floor(h);
			f = h - i; 
			p = v * (1 - s);
			q = v * (1 - s * f);
			t = v * (1 - s * (1 - f));
		 
			switch(i) {
				case 0:
					r = v;
					g = t;
					b = p;
					break;
		 
				case 1:
					r = q;
					g = v;
					b = p;
					break;
		 
				case 2:
					r = p;
					g = v;
					b = t;
					break;
		 
				case 3:
					r = p;
					g = q;
					b = v;
					break;
		 
				case 4:
					r = t;
					g = p;
					b = v;
					break;
		 
				default: // case 5:
					r = v;
					g = p;
					b = q;
			}
			
			r = Math.round(255*r);
			g = Math.round(255*g);
			b = Math.round(255*b);
		}
		
		return ((int)r << 16) | ((int)g << 8) | (int)b;
	}
	
	public void smooth_likelihood(int x, int y, int z, double smooth, double [][][] likelihoods) {
    	for(int i =0; i < x; i++){
            for(int j =0; j < y; j++){
                for(int k =0; k < z; k++){
                    likelihoods[i][j][k] = smooth;
                }
            }                        	
    	}
	}
	
    public void update_likelihood(double [] freq, double [] arr, double total, double smooth, 
    		double [][][] likelihoods, int x, int y, int z, int type) {
        for(int i =0; i < x; i++){	
        	freq[i]= (double)(arr[i])/(total);
        	
            for(int j =0; j < y; j++){
                for(int k =0; k < z; k++){
                    likelihoods[i][j][k] = (double)(likelihoods[i][j][k])/(arr[i]+(2*smooth) );
                }
            }
            //print digits only if digit classification
            if(type == 1) {
            	System.out.println("Num "+ i+ ": " + arr[i]);
            }
        }
    }
    
    public int pick(int x, int y, int z,  double[] freq, double [][] image, double [][][] likelihoods) {
    	
        double [] probabilities = new double [x];
        int best = 0;
        
        for(int i =0; i < x; i++){
        	probabilities[i]= probabilities[i] + (Math.log(freq[i]) );
            for(int j =0; j < y; j++){
                for(int k =0; k < z; k++){
                    if(image[j][k]==0) {
                    	probabilities[i] = probabilities[i] + ( Math.log(1-likelihoods[i][j][k]) );
                    }
                    else {
                    	probabilities[i] = probabilities[i] + ( Math.log(likelihoods[i][j][k]) );
                    }
                }

            }
            if(probabilities[i] > probabilities[best]) {
                   best = i;
            }
        }
        return best;
    }
    
    public BufferedImage likelihood_map(int x, int y, int z, int c, double[][][] likelihoods) {
		BufferedImage img = new BufferedImage(z, y, BufferedImage.TYPE_INT_RGB);
		NaiveBayes colors = new NaiveBayes();
		double num, min = 100, max = -100;
		for (int i = 0; i < z; i++) {
			for (int j = 0; j < y; j++) {
				num = Math.log(likelihoods[c][j][i]);
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
				num = Math.log(likelihoods[c][j][i]);
				img.setRGB(i, j, colors.get_color(num, min, max));
			}
		}

		return img;
    }
    
	public void print_confusion_matrix(int x, double [][] confusion) {
		int sum = 0;
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < x; j++) {
				sum += confusion[j][i];
			}
			for (int j = 0; j < x; j++) {
				confusion[j][i] = confusion[j][i]/sum;
			}
			sum = 0;
		}
		
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < x; j++) {
				if ((100*(confusion[i][j])) >= 10)
					System.out.printf("%.1f ",(float)(100*(confusion[i][j])));
				else
					System.out.printf("%.1f  ",(float)(100*(confusion[i][j])));
			}
			System.out.println();
		}
		System.out.println();
	}
}
