package com.example.medic;

import android.R.string;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;


import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.PorterDuff;

//logic for main section of Android study-card application
public class StudyPage extends Activity {
	private Button b1;
	private Button b2;
	private Button b3;
	private Button b4;
	private Button back;
	QAndAs[] questionArray;
	QAndAs current_q;
	int score = 0;
    public static Context context;
    int gameplayCounter = 0;
    private TextView correct;
	
    
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_study_page);
		Bundle extras = getIntent().getExtras();  
		String amALevel = extras.getString("level");
		setup(amALevel);
		
		back = (Button)findViewById(R.id.backButton);
		back.setOnClickListener(onBack);
		
        
	}
    
	private View.OnClickListener onBack=new View.OnClickListener() {

	    @Override
	    public void onClick(View v) {
	        Intent myIntent=new Intent(v.getContext(),StudyLevelsPage.class );
	        startActivity(myIntent);
	        finish();

	    }
	};
    
   public void setup(String questionLevel)
    {
    	b1 = (Button)findViewById(R.id.button1);
    	b2 = (Button)findViewById(R.id.button2);
    	b3 = (Button)findViewById(R.id.button3);
    	b4 = (Button)findViewById(R.id.button4);
        
    	context = getApplicationContext();	
    	b1.setOnClickListener(clickLis);
    	b2.setOnClickListener(clickLis);
    	b3.setOnClickListener(clickLis);
    	b4.setOnClickListener(clickLis);
    		
    		
            questionArray = new QAndAs[20];
            
            for(int i =0; i<questionArray.length; i++)
            {
                try{
 
               AssetManager assetMan = context.getAssets();              
               InputStream is = assetMan.open(questionLevel);
               BufferedReader buffRead = new BufferedReader(new InputStreamReader(is, "UTF-8"));
               String thisline = buffRead.readLine();
                
                while (thisline != null){
    //reads in and assigns question from file
                    questionArray[i] = new QAndAs();
                    questionArray[i].setTheQuestion(thisline);

                    thisline = buffRead.readLine();
    //reads in and assigns choices                
                    questionArray[i].setC1(thisline);
     
                    thisline = buffRead.readLine();
                    if(thisline.contentEquals("true"))
                    {
                        questionArray[i].setC1true();
                    }
                    
                    thisline = buffRead.readLine();
                    
                    questionArray[i].setC2(thisline);
                    thisline = buffRead.readLine();
                    if(thisline.contentEquals("true"))
                    {
                        questionArray[i].setC2true();
               
                    }
                    thisline = buffRead.readLine();
                    
                   questionArray[i].setC3(thisline);
              
                    thisline = buffRead.readLine();
                    if(thisline.contentEquals("true"))
                    {
                        questionArray[i].setC3true();
              
                    }
                    thisline = buffRead.readLine();
                    
                    questionArray[i].setC4(thisline);
          
                    thisline = buffRead.readLine();
                    if(thisline.contentEquals("true"))
                    {
                        questionArray[i].setC4true();
          
                    }
                    thisline = buffRead.readLine();
                    i++;
                    
                }
                
                buffRead.close();
                
            }
                catch(FileNotFoundException e){
                    System.out.println("File opening problem.");
                }   
            catch(IOException e){
                System.out.println("Error reading from file");
            }
     
            }  
            gameplay();
    }
    
	
	//selects a random question from the array	
	
	public void gameplay()
	{
		
		Random rand = new Random();
        int randomQuestion;
        randomQuestion = rand.nextInt(20);
        current_q = questionArray[randomQuestion];
        String currentQuestion = current_q.getQuestion();
        TextView tv = (TextView)findViewById(R.id.textView1);
        tv.setText(currentQuestion);
        
        String currentc1 = questionArray[randomQuestion].getChoice1();
        b1.setText(currentc1);
        
        String currentc2 = questionArray[randomQuestion].getChoice2();
        b2.setText(currentc2);
        
        String currentc3 = questionArray[randomQuestion].getChoice3();
        b3.setText(currentc3);
        
        String currentc4 = questionArray[randomQuestion].getChoice4();
        b4.setText(currentc4);
        		
		}
	
	
	View.OnClickListener clickLis = new View.OnClickListener()
	{
		@Override
		public void onClick(View arg0)
		{
			ifClicked((Button) arg0);
		}
	};

	//checks if answer was correct
	private boolean correct_answer(Button b) {
		return b.equals(b1) && current_q.getC1bool() == true || b.equals(b2) && current_q.getC2bool() || b.equals(b3) && current_q.getC3bool() || b.equals(b4) && current_q.getC4bool();
	}
	
	public void ifClicked(Button b)
	{
		correct = (TextView)findViewById(R.id.textView3);
	
		
		if(current_q == null ) {
			System.out.println("Current question is null");
			return;}
        //lets user know if previous answer was correct or incorrect
		if (correct_answer(b) == true){
			correct.setText("Correct!");
			correct.setTextColor(Color.GREEN);
			gameplay();
			return;
		}
		else 
		{	
			correct.setText("Incorrect!");
			correct.setTextColor(Color.RED);
			gameplay();
			return;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.study_page, menu);
		return true;
	}

}




