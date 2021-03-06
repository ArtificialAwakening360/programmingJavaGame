import javax.swing.JComponent;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Rectangle;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

public class Draw extends JComponent implements ActionListener{

	public boolean beginGame = false;
	public Rectangle buttonOne;
	public int button1_x = 170;
	public int button1_y = 150;
	public int button1_width = 200;
	public int button1_height = 50;

	public Mouse mouse;
	public Rectangle mouseBounds;

	public Rectangle buttonTwo;
	public int button2_x = 170;
	public int button2_y = 250;
	public int button2_width = 200;
	public int button2_height = 50;

	private final int gravity = 370;
	private final int x = 50;
	private BufferedImage backgroundImage;
	private URL backgroundFile = getClass().getResource("day.png");

	public Player player;
	public int playerX = 20;
	public int playerY = 60;

	public MagicAmount magic[];
	public ArrayList<MagicAmount>magicList = new ArrayList<>();
	public int magicCapacity = 1;
	public MagicMissile magicMissile;
	public int magicX = 55;
	public int magicY = 48;
	public int bMagicX = 65;
	public int bMagicY = 57;

	public Monster monster;
	public ArrayList<Monster> monsterList = new ArrayList<>();
	public int counter = 1;
	public Timer timer = new Timer(20000, this);

	public boolean magicHits = false;

	public Draw(){
		timer.start();
		spawnPlayer();
		spawnMonster();
		
		mouse = new Mouse();
		addMouseListener(mouse);
		addMouseMotionListener(mouse);

		try{
			backgroundImage = ImageIO.read(backgroundFile);
		}
		catch(IOException e){
			e.printStackTrace();
		}
		startGame();
	}

	public void spawnPlayer(){
		player = new Player(x, gravity, this);
		magic = new MagicAmount[10];

		for(int p = 0; p < 10; p++){
			magic[p] = new MagicAmount(magicX, magicY, this);
			magicList.add(magic[p]);
			magicX+=20;
		}
	}

	public void spawnMonster(){
		int spawnPostion = 510;
		if(monsterList.size() != 10){
			monster = new Monster(spawnPostion, gravity + 10, this);
			monsterList.add(monster);
			counter++;
		}	
	}

	public void paintComponent(Graphics g){
		super.paintComponent(g);
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, 600, 500);
			g.drawImage(backgroundImage, 0, 0, this);

			g.drawImage(player.image, player.xPos, player.yPos, this);

			loadPlayerStats(g);

			for(int m = 0; m <player.missileList.size(); m++ ){
				if(player.isUsingMagic == true){
					if(player.missileList.size() != 0){	
						g.drawImage(player.missileList.get(m).magicImage,player.missileList.get(m).magicX, player.missileList.get(m).magicY, this);	
					}
				}
			}

			for(int r = 0; r < monsterList.size(); r++){
				if(monsterList.size() != 0){
					g.drawImage(monsterList.get(r).monsterImage, monsterList.get(r).xPos, monsterList.get(r).yPos, this);
					g.setColor(Color.RED);
					g.fillRect(monsterList.get(r).xPos, monsterList.get(r).yPos, 35, 2);
					g.setColor(Color.GREEN);
					g.fillRect(monsterList.get(r).xPos, monsterList.get(r).yPos, monsterList.get(r).hp, 2);
				}		
			}
	}

	public void loadPlayerStats(Graphics g){

		String health = String.valueOf(player.healthBar);
		g.setColor(Color.BLACK);
		g.fillRect(6, 18, 264, 34);
		g.setColor(Color.DARK_GRAY);
		g.fillRect(8, 20, 260, 30);

		g.setColor(Color.MAGENTA);
		g.fillRect(6, 53, 264, 24);
		g.setColor(Color.DARK_GRAY);
		g.fillRect(8, 55, 260, 20);

		g.setColor(Color.RED);
		g.fillRect(50, 25, 200, 10);
		g.setColor(Color.GREEN);
		g.fillRect(50, 25, player.healthBar, 10);

		g.setColor(Color.GREEN);
		g.setFont(new Font("Arial", Font.BOLD, 10));
		g.drawString("Health:", 10, 34);
		g.setColor(Color.GREEN);
		g.setFont(new Font("Arial", Font.BOLD, 10));
		g.drawString(health + "/200", 60, 47);

		g.setColor(Color.MAGENTA);
		g.setFont(new Font("Arial", Font.BOLD, 10));
		g.drawString("Magic:", 10, 66);

		for(int m = 0; m < magicList.size(); m++){
			g.drawImage(magicList.get(m).magicImage, magicList.get(m).xPos, magicList.get(m).yPos, this);
		}
	}

	public void startGame(){
		Thread startThread = new Thread(new Runnable(){
			public void run(){
				try{	
					while(true){
						
						for(int m = 0; m < monsterList.size(); m++){

							if(monsterList.get(m).checkHealth() == true){
								monsterList.get(m).monsterDeath();
								collisionDetection();
								eraseImages();
							} 
							if(monsterList.get(m).isAttacking != true){
								monsterList.get(m).movementMonster(player);
								collisionDetection();

							}else {
								monsterList.get(m).attackMonster(player);
								System.out.println("monster attack");
								collisionDetection();
								damageDetection();

							}
						}
						Thread.sleep(350);
					}
				}catch(ArrayIndexOutOfBoundsException e){
					e.printStackTrace();
					spawnMonster();
					
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
		});
		startThread.start();
	}

	public void actionPerformed(ActionEvent arg0) {
		spawnMonster();	
	}

	public void collisionDetection(){
		
		for(int c = 0; c < monsterList.size(); c++){
			Rectangle playerBounds = player.playerBounds();
			Rectangle monsterBounds = monsterList.get(c).monsterBounds();
			
			if(monsterList.size() != 0){
				if(playerBounds.intersects(monsterBounds)){
					monsterList.get(c).isAttacking = true;
					System.out.println("INTERSECT MONSTERS");
				
				}else{
					monsterList.get(c).isAttacking = false;
					monsterList.get(c).isMoving = true;
				}
				if(player.useSword == true){
					if(playerBounds.intersects(monsterBounds)){
						System.out.println("Player attacks");
						monsterList.get(c).hp-=player.power;
					}
				}
				for(MagicMissile playerMagic: player.missileList){
					if(playerMagic.magicBounds().intersects(monsterBounds)){
						playerMagic.magicHit();
						damageDetection();
						eraseImages();
						break;
					}
				}
			}
		}
	}

	public void damageDetection(){

		for(int m = 0; m < monsterList.size(); m ++){
			for(int p = 0; p < player.missileList.size(); p++){
				System.out.println("Assessing");
				
				if(player.missileList.get(p).missileImpact == true){
					System.out.println("Assessing Magic Damage");
					monsterList.get(p).hp-=player.missileList.get(p).magicDmg;
				}	
			}
		}

		for(int m = 0; m < monsterList.size(); m ++){
			if(monsterList.get(m).isAttacking == true){
				player.healthBar = player.healthBar - (monsterList.get(m).atk / player.defense);
				System.out.println("Assessing Monster Damage");
			}
		}
	}

	public void eraseImages(){
		
		for(int e = 0; e < monsterList.size(); e++){
			if(monsterList.get(e).hp <= 0 ){
				monsterList.remove(e);
				System.out.println("monster deleted");
			}
		}
	
		for(int e = 0; e < player.missileList.size(); e++){
			if(player.missileList.get(e).missileImpact == true ||player.missileList.get(e).magicX >= 550){
				System.out.println("magic deletion");
				player.missileList.remove(e);			
			}
		}
		
		for(int e = 0; e < magicList.size(); e++){
			if(player.isUsingMagic == true){
				if(magicList.contains(magicList.get(e))){
					magicList.remove(magicList.remove(e));
					System.out.println("Magic orb Deletion");
					break;
				}
			}
		}
	}
}