package com.rhwteam.yeyaowei.NewMenu;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.GuiModList;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonLanguage;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.ForgeHooksClient;
import org.apache.commons.io.Charsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.Project;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

@SideOnly(Side.CLIENT)
public class NewMenu extends GuiScreen implements GuiYesNoCallback
{
    private static final Logger logger = LogManager.getLogger();
    private static final Random rand = new Random();
    private float updateCounter;
    private String splashText;
    private GuiButton buttonResetDemo;
    private int panoramaTimer;
    private DynamicTexture viewportTexture;
    private final Object rectDownload_t = new Object();
    private String clickChangelog;
    private String newVersionDetected;
    private static String downloadUrl;
    private static String newVersion;
    private static final ResourceLocation splashTexts = new ResourceLocation("texts/splashes.txt");
    private static final ResourceLocation minecraftTitleTextures = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation[] titlePanoramaPaths = new ResourceLocation[] {new ResourceLocation("textures/gui/title/background/panorama_0.png"), new ResourceLocation("textures/gui/title/background/panorama_1.png"), new ResourceLocation("textures/gui/title/background/panorama_2.png"), new ResourceLocation("textures/gui/title/background/panorama_3.png"), new ResourceLocation("textures/gui/title/background/panorama_4.png"), new ResourceLocation("textures/gui/title/background/panorama_5.png")};
    private int rectDownload_u;
    private int rectDownload_w;
    private ResourceLocation field_110351_G;
    private static final String __OBFID = "CL_00001154";
    public static String maxplayer, onlineplayer;
    private static String online, ping;
    private static ServerData server = new ServerData("Server", ConfigVar.ServerAddress);
    private static ServerData server1 = new ServerData("Server1", ConfigVar.ServerAddress1);
    private final NewServerPinger oldServerPinger = new NewServerPinger();
    private GuiButton btns;
    private GuiButton btns1;
    private static final ThreadPoolExecutor field_148302_b = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());

    public NewMenu()
    {
        this.splashText = "missingno";
        BufferedReader bufferedreader = null;

        try
        {
            ArrayList arraylist = new ArrayList();
            bufferedreader = new BufferedReader(new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(splashTexts).getInputStream(), Charsets.UTF_8));
            String s;

            while ((s = bufferedreader.readLine()) != null)
            {
                s = s.trim();

                if (!s.isEmpty())
                {
                    arraylist.add(s);
                }
            }

            if (!arraylist.isEmpty())
            {
                do
                {
                    this.splashText = (String)arraylist.get(rand.nextInt(arraylist.size()));
                }
                while (this.splashText.hashCode() == 125780783);
            }
        }
        catch (IOException ioexception1)
        {
            ;
        }
        finally
        {
            if (bufferedreader != null)
            {
                try
                {
                    bufferedreader.close();
                }
                catch (IOException ioexception)
                {
                    ;
                }
            }
        }

        this.updateCounter = rand.nextFloat();
        field_148302_b.submit(new Runnable()
    	{
    		private static final String __OBFID = "CL_00000818";
    		public void run()
    		{
    			try
    			{
    				NewMenu.this.getOldServerPinger().func_147224_a(NewMenu.this.server);
    			}
    			catch (UnknownHostException unknownhostexception)
    			{
    				NewMenu.this.server.pingToServer = -1L;
    				NewMenu.this.server.serverMOTD = "§c§l网络错误";
    			}
    			catch (Exception exception)
    			{
    				NewMenu.this.server.pingToServer = -1L;
    				NewMenu.this.server.serverMOTD = "§c§l网络错误";
    			}
    		}
    	});
        FMLClientHandler.instance().setupServerList();
    }
    public void updateScreen()
    {
        ++this.panoramaTimer;
    }

    public boolean doesGuiPauseGame()
    {
    	//主界面怎么可能要暂停(⊙﹏⊙)b
        return false;
    }

    public void initGui()
    {
        this.viewportTexture = new DynamicTexture(256, 256);
        this.field_110351_G = this.mc.getTextureManager().getDynamicTextureLocation("background", this.viewportTexture);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        if (calendar.get(2) + 1 == 11 && calendar.get(5) == 9)
        {
            this.splashText = "Happy birthday, ez!";
        }
        else if (calendar.get(2) + 1 == 6 && calendar.get(5) == 1)
        {
            this.splashText = "Happy birthday, Notch!";
        }
        else if (calendar.get(2) + 1 == 12 && calendar.get(5) == 24)
        {
            this.splashText = "Merry X-mas!";
        }
        else if (calendar.get(2) + 1 == 1 && calendar.get(5) == 1)
        {
            this.splashText = "Happy new year!";
        }
        else if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31)
        {
            this.splashText = "OOoooOOOoooo! Spooky!";
        }

        boolean flag = true;
        int i = this.height / 4 + 48;

        if (this.mc.isDemo())
        {
            this.addDemoButtons(i, 24);
        }
        else
        {
            this.addSingleplayerMultiplayerButtons(i, 24);
        }

        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, i + 72 + 12, 98, 20, I18n.format("menu.options", new Object[0])));
        this.buttonList.add(new GuiButton(4, this.width / 2 + 2, i + 72 + 12, 98, 20, I18n.format("menu.quit", new Object[0])));
        this.buttonList.add(new GuiButtonLanguage(20, this.width / 2 - 124, i + 72 + 12));
        synchronized (this.rectDownload_t)
        {
            this.rectDownload_u = ((GuiButton)this.buttonList.get(0)).yPosition - 24;
            this.rectDownload_w = this.rectDownload_u + 24;
        }
    }

    private void addSingleplayerMultiplayerButtons(int p_73969_1_, int p_73969_2_)
    {
    	if (ConfigVar.IsTwoAddress)
    	{
    		this.buttonList.add(this.btns = new GuiButton(21, this.width / 2 + 2, p_73969_1_, 98, 20,I18n.format("服务器入口I", new Object[0])));
    		this.buttonList.add(this.btns1 = new GuiButton(22, this.width / 2 + 2, p_73969_1_ + p_73969_2_ * 1, 98, 20,I18n.format("服务器入口II", new Object[0])));
        	this.buttonList.add(new GuiButton(1, this.width / 2 + 2, p_73969_1_ + p_73969_2_ * 2, 98, 20, I18n.format("单人游戏", new Object[0])));
        	if (isOutdate())
            {
                this.newVersionDetected = I18n.format("§e§l检测到新版本 §b§l" + newVersion, new Object[0]);
                this.clickChangelog = I18n.format("§a§l点击此处查看更新记录", new Object[0]);
                this.btns.enabled = false;
                this.btns1.enabled = false;
                this.btns.displayString = "客户端版本过期";
                this.btns1.displayString = "客户端版本过期";
            }
        	else if(!getServerStatus())
        	{
                this.btns.enabled = false;
                this.btns1.enabled = false;
                this.btns.displayString = "服务器维护中";
                this.btns1.displayString = "服务器维护中";
        	}
    	}
    	else
    	{
    		this.buttonList.add(this.btns = new GuiButton(21, this.width / 2 + 2, p_73969_1_, 98, 20,I18n.format("进入服务器", new Object[0])));
        	this.buttonList.add(new GuiButton(1, this.width / 2 + 2, p_73969_1_ + p_73969_2_ * 1, 98, 20, I18n.format("单人游戏", new Object[0])));
        	if (isOutdate())
            {
                this.newVersionDetected = I18n.format("§e§l检测到新版本 §b§l" + newVersion, new Object[0]);
                this.clickChangelog = I18n.format("§a§l点击此处查看更新记录", new Object[0]);
                this.btns.enabled = false;
                this.btns.displayString = "客户端版本过期";
            }
        	else if(!getServerStatus())
        	{
                this.btns.enabled = false;
                this.btns.displayString = "服务器维护中";
        	}
        	this.buttonList.add(new GuiButton(6, this.width / 2 + 2, p_73969_1_ + p_73969_2_ * 2, 98, 20, I18n.format("Mods", new Object[0])));
    	}
    }

    private void addDemoButtons(int p_73972_1_, int p_73972_2_)
    {
        this.buttonList.add(new GuiButton(11, this.width / 2 - 100, p_73972_1_, I18n.format("menu.playdemo", new Object[0])));
        this.buttonList.add(this.buttonResetDemo = new GuiButton(12, this.width / 2 - 100, p_73972_1_ + p_73972_2_ * 1, I18n.format("menu.resetdemo", new Object[0])));
        ISaveFormat isaveformat = this.mc.getSaveLoader();
        WorldInfo worldinfo = isaveformat.getWorldInfo("Demo_World");

        if (worldinfo == null)
        {
            this.buttonResetDemo.enabled = false;
        }
    }

    protected void actionPerformed(GuiButton p_146284_1_)
    {
        if (p_146284_1_.id == 0)
        {
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
        }

        if (p_146284_1_.id == 5)
        {
            this.mc.displayGuiScreen(new GuiLanguage(this, this.mc.gameSettings, this.mc.getLanguageManager()));
        }

        if (p_146284_1_.id == 1)
        {
            this.mc.displayGuiScreen(new GuiSelectWorld(this));
        }

        if (p_146284_1_.id == 2)
        {
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
        }

        if (p_146284_1_.id == 14)
        {
            this.func_140005_i();
        }

        if (p_146284_1_.id == 4)
        {
            this.mc.shutdown();
        }

        if (p_146284_1_.id == 6)
        {
            this.mc.displayGuiScreen(new GuiModList(this));
        }

        if (p_146284_1_.id == 11)
        {
            this.mc.launchIntegratedServer("Demo_World", "Demo_World", DemoWorldServer.demoWorldSettings);
        }

        if (p_146284_1_.id == 12)
        {
            ISaveFormat isaveformat = this.mc.getSaveLoader();
            WorldInfo worldinfo = isaveformat.getWorldInfo("Demo_World");

            if (worldinfo != null)
            {
                GuiYesNo guiyesno = GuiSelectWorld.func_152129_a(this, worldinfo.getWorldName(), 12);
                this.mc.displayGuiScreen(guiyesno);
            }
        }
        
        if (p_146284_1_.id == 20)
        {
        	this.getOldServerPinger().func_147226_b();
        	field_148302_b.submit(new Runnable()
            {
                private static final String __OBFID = "CL_00000818";
                public void run()
                {
                    try
                    {
                    	NewMenu.this.getOldServerPinger().func_147224_a(NewMenu.this.server);
                    }
                    catch (UnknownHostException var2)
                    {
                    	NewMenu.this.server.pingToServer = -1L;
                    	NewMenu.this.server.serverMOTD = "§4§l网络错误";
                    }
                    catch (Exception var3)
                    {
                    	NewMenu.this.server.pingToServer = -1L;
                    	NewMenu.this.server.serverMOTD = "§4§l网络错误";
                    }
                }
            });
        	FMLClientHandler.instance().setupServerList();
        }
        
        if (p_146284_1_.id == 21)
        {
        	FMLClientHandler.instance().setupServerList();
        	FMLClientHandler.instance().connectToServer(this, server);
        }
        
        if (p_146284_1_.id == 22)
        {
        	FMLClientHandler.instance().setupServerList();
        	FMLClientHandler.instance().connectToServer(this, server1);
        }
        
    }

    private void func_140005_i()
    {
        RealmsBridge realmsbridge = new RealmsBridge();
        realmsbridge.switchToRealms(this);
    }

    public void confirmClicked(boolean p_73878_1_, int p_73878_2_)
    {
        if (p_73878_1_ && p_73878_2_ == 12)
        {
            ISaveFormat isaveformat = this.mc.getSaveLoader();
            isaveformat.flushCache();
            isaveformat.deleteWorldDirectory("Demo_World");
            this.mc.displayGuiScreen(this);
        }
        else if (p_73878_2_ == 13)
        {
            if (p_73878_1_)
            {
                try
                {
                    Class oclass = Class.forName("java.awt.Desktop");
                    Object object = oclass.getMethod("getDesktop", new Class[0]).invoke((Object)null, new Object[0]);
                    oclass.getMethod("browse", new Class[] {URI.class}).invoke(object, new Object[] {new URI(this.downloadUrl)});
                }
                catch (Throwable throwable)
                {
                    logger.error("Couldn\'t open link", throwable);
                }
            }

            this.mc.displayGuiScreen(this);
        }
    }

    private void drawPanorama(int p_73970_1_, int p_73970_2_, float p_73970_3_)
    {
        Tessellator tessellator = Tessellator.instance;
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        Project.gluPerspective(120.0F, 1.0F, 0.05F, 10.0F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        byte b0 = 8;

        for (int k = 0; k < b0 * b0; ++k)
        {
            GL11.glPushMatrix();
            float f1 = ((float)(k % b0) / (float)b0 - 0.5F) / 64.0F;
            float f2 = ((float)(k / b0) / (float)b0 - 0.5F) / 64.0F;
            float f3 = 0.0F;
            GL11.glTranslatef(f1, f2, f3);
            GL11.glRotatef(MathHelper.sin(((float)this.panoramaTimer + p_73970_3_) / 400.0F) * 25.0F + 20.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(-((float)this.panoramaTimer + p_73970_3_) * 0.1F, 0.0F, 1.0F, 0.0F);

            for (int l = 0; l < 6; ++l)
            {
                GL11.glPushMatrix();

                if (l == 1)
                {
                    GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                }

                if (l == 2)
                {
                    GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                }

                if (l == 3)
                {
                    GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
                }

                if (l == 4)
                {
                    GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (l == 5)
                {
                    GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                }

                this.mc.getTextureManager().bindTexture(titlePanoramaPaths[l]);
                tessellator.startDrawingQuads();
                tessellator.setColorRGBA_I(16777215, 255 / (k + 1));
                float f4 = 0.0F;
                tessellator.addVertexWithUV(-1.0D, -1.0D, 1.0D, (double)(0.0F + f4), (double)(0.0F + f4));
                tessellator.addVertexWithUV(1.0D, -1.0D, 1.0D, (double)(1.0F - f4), (double)(0.0F + f4));
                tessellator.addVertexWithUV(1.0D, 1.0D, 1.0D, (double)(1.0F - f4), (double)(1.0F - f4));
                tessellator.addVertexWithUV(-1.0D, 1.0D, 1.0D, (double)(0.0F + f4), (double)(1.0F - f4));
                tessellator.draw();
                GL11.glPopMatrix();
            }

            GL11.glPopMatrix();
            GL11.glColorMask(true, true, true, false);
        }

        tessellator.setTranslation(0.0D, 0.0D, 0.0D);
        GL11.glColorMask(true, true, true, true);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private void rotateAndBlurSkybox(float p_73968_1_)
    {
        this.mc.getTextureManager().bindTexture(this.field_110351_G);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, 256, 256);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColorMask(true, true, true, false);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        byte b0 = 3;

        for (int i = 0; i < b0; ++i)
        {
            tessellator.setColorRGBA_F(1.0F, 1.0F, 1.0F, 1.0F / (float)(i + 1));
            int j = this.width;
            int k = this.height;
            float f1 = (float)(i - b0 / 2) / 256.0F;
            tessellator.addVertexWithUV((double)j, (double)k, (double)this.zLevel, (double)(0.0F + f1), 1.0D);
            tessellator.addVertexWithUV((double)j, 0.0D, (double)this.zLevel, (double)(1.0F + f1), 1.0D);
            tessellator.addVertexWithUV(0.0D, 0.0D, (double)this.zLevel, (double)(1.0F + f1), 0.0D);
            tessellator.addVertexWithUV(0.0D, (double)k, (double)this.zLevel, (double)(0.0F + f1), 0.0D);
        }

        tessellator.draw();
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColorMask(true, true, true, true);
    }

    private void renderSkybox(int p_73971_1_, int p_73971_2_, float p_73971_3_)
    {
        this.mc.getFramebuffer().unbindFramebuffer();
        GL11.glViewport(0, 0, 256, 256);
        this.drawPanorama(p_73971_1_, p_73971_2_, p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.mc.getFramebuffer().bindFramebuffer(true);
        GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        float f1 = this.width > this.height ? 120.0F / (float)this.width : 120.0F / (float)this.height;
        float f2 = (float)this.height * f1 / 256.0F;
        float f3 = (float)this.width * f1 / 256.0F;
        tessellator.setColorRGBA_F(1.0F, 1.0F, 1.0F, 1.0F);
        int k = this.width;
        int l = this.height;
        tessellator.addVertexWithUV(0.0D, (double)l, (double)this.zLevel, (double)(0.5F - f2), (double)(0.5F + f3));
        tessellator.addVertexWithUV((double)k, (double)l, (double)this.zLevel, (double)(0.5F - f2), (double)(0.5F - f3));
        tessellator.addVertexWithUV((double)k, 0.0D, (double)this.zLevel, (double)(0.5F + f2), (double)(0.5F - f3));
        tessellator.addVertexWithUV(0.0D, 0.0D, (double)this.zLevel, (double)(0.5F + f2), (double)(0.5F + f3));
        tessellator.draw();
    }
    
    private void drawAnnouncement() {
		if (field_148302_b != null) {
			online = server.serverMOTD;
			ping = "§a§l" + server.pingToServer;
		} else {
			ping = "§4§l0";
		}
	}
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_)
    {
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        this.renderSkybox(p_73863_1_, p_73863_2_, p_73863_3_);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        Tessellator tessellator = Tessellator.instance;
        short short1 = 274;
        int k = this.width / 2 - short1 / 2;
        byte b0 = 30;
        this.drawGradientRect(0, 0, this.width, this.height, -2130706433, 16777215);
        this.drawGradientRect(0, 0, this.width, this.height, 0, Integer.MIN_VALUE);
        this.mc.getTextureManager().bindTexture(minecraftTitleTextures);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        if ((double)this.updateCounter < 1.0E-4D)
        {
            this.drawTexturedModalRect(k + 0, b0 + 0, 0, 0, 99, 44);
            this.drawTexturedModalRect(k + 99, b0 + 0, 129, 0, 27, 44);
            this.drawTexturedModalRect(k + 99 + 26, b0 + 0, 126, 0, 3, 44);
            this.drawTexturedModalRect(k + 99 + 26 + 3, b0 + 0, 99, 0, 26, 44);
            this.drawTexturedModalRect(k + 155, b0 + 0, 0, 45, 155, 44);
        }
        else
        {
            this.drawTexturedModalRect(k + 0, b0 + 0, 0, 0, 155, 44);
            this.drawTexturedModalRect(k + 155, b0 + 0, 0, 45, 155, 44);
        }

        tessellator.setColorOpaque_I(-1);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(this.width / 2 + 90), 70.0F, 0.0F);
        GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
        float f1 = 1.8F - MathHelper.abs(MathHelper.sin((float)(Minecraft.getSystemTime() % 1000L) / 1000.0F * (float)Math.PI * 2.0F) * 0.1F);
        f1 = f1 * 100.0F / (float)(this.fontRendererObj.getStringWidth(this.splashText) + 32);
        GL11.glScalef(f1, f1, f1);
        //-256
        this.drawCenteredString(this.fontRendererObj, this.splashText, 0, -8, -256);
        GL11.glPopMatrix();
        String s = "Minecraft 1.7.10";

        if (this.mc.isDemo())
        {
            s = s + " Demo";
        }

        List<String> brandings = Lists.reverse(FMLCommonHandler.instance().getBrandings(true));
        for (int i = 0; i < brandings.size(); i++)
        {
            String brd = brandings.get(i);
            if (!Strings.isNullOrEmpty(brd))
            {
                this.drawString(this.fontRendererObj, brd, 2, this.height - ( 10 + i * (this.fontRendererObj.FONT_HEIGHT + 1)), 16777215);
            }
        }

        String s1 = "§bMojang AB.版权所有";
        this.drawString(this.fontRendererObj, s1, this.width - this.fontRendererObj.getStringWidth(s1) - 2, this.height - 10, -1);
        this.drawString(this.fontRendererObj, "§e§lUI制作 BakaSea(SPE_HaiKuo)", this.width - this.fontRendererObj.getStringWidth("§e§lUI制作 BakaSea(SPE_HaiKuo)") - 2, this.height - 20, -1);
        this.drawString(this.fontRendererObj, "§a§lUI修改 R9 Nano(yeyaowei)", this.width - this.fontRendererObj.getStringWidth("§a§lUI修改 R9 Nano(yeyaowei)") - 2, this.height - 30, -1);
        if(ConfigVar.debug)
        {
        	this.drawString(this.fontRendererObj, "§cDEBUG 模式", this.width - this.fontRendererObj.getStringWidth("§cDEBUG 模式") - 2, this.height - 40, -1);
        }
        drawRect(0,0,800,12,0x80000000);
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestring = "§e"+time.format(new Date());
		this.drawString(this.fontRendererObj, timestring, 1, 2, -1);
		ConfigVar.announcement = this.getAnnouncement();
		this.drawString(this.fontRendererObj, ConfigVar.announcement, (this.width / 2 - ConfigVar.announcement.length() * 2), 2, -1);
		int varinfow = this.width / 2 - 119;
        int varinfoh = this.height / 4 + 48;
        drawAnnouncement();
        this.drawGradientRect(varinfow - 4, varinfoh, varinfow + 117, varinfoh + 70, 1610612736, 1610612736);
        this.drawString(this.fontRendererObj, "§e==========公告==========", varinfow, varinfoh,
                        16777215);
        this.drawString(this.fontRendererObj, "§e服务器状态: " + online, varinfow, varinfoh + 12, 16777215);
        this.drawString(this.fontRendererObj, "§e在线人数: " + onlineplayer, varinfow, varinfoh + 24, 16777215);
        this.drawString(this.fontRendererObj, "§e最大人数: " + maxplayer, varinfow, varinfoh + 36, 16777215);
        this.drawString(this.fontRendererObj, "§e与服务器延迟: " + ping, varinfow, varinfoh + 48, 16777215);
        this.drawString(this.fontRendererObj, "§e祝您游戏愉快'_>'", varinfow, varinfoh + 60, 16777215);
        
        if (this.newVersionDetected != null && this.newVersionDetected.length() > 0)
        {
            drawRect(varinfow - 4, this.rectDownload_u - 2, varinfow + 117, this.rectDownload_w - 1, 1428160512);
            drawRect(this.width /2 + 2, this.rectDownload_u -2, this.width / 2 + 100, this.rectDownload_w - 1, 1428160512);
            int rectHeight = (this.rectDownload_w - this.rectDownload_u) / 2;
            this.drawString(this.fontRendererObj, this.newVersionDetected, varinfow, this.rectDownload_u, -1);
            this.drawString(this.fontRendererObj, this.clickChangelog, varinfow, ((GuiButton)this.buttonList.get(0)).yPosition - 12, -1);

            this.drawCenteredString(fontRendererObj, "§e§l[ 打开下载地址 ]", this.width /2 + 52, this.rectDownload_u + rectHeight - 5, -1);
            
        }

        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }


    protected void mouseClicked(int p_73864_1_, int p_73864_2_, int p_73864_3_)
    {
        super.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);
        Object object = this.rectDownload_t;

        synchronized (this.rectDownload_t)
        {
        	int varinfow = this.width / 2 - 119;
            if (this.newVersionDetected != null && this.newVersionDetected.length() > 0 && p_73864_1_ >= this.width /2 + 2 && p_73864_1_ <= this.width / 2 + 100 && p_73864_2_ >= this.rectDownload_u && p_73864_2_ <= this.rectDownload_w)
            {
                GuiConfirmOpenLink guiconfirmopenlink = new GuiConfirmOpenLink(this, this.downloadUrl, 13, true);
                guiconfirmopenlink.func_146358_g();
                this.mc.displayGuiScreen(guiconfirmopenlink);
            }
            if (this.newVersionDetected != null && this.newVersionDetected.length() > 0 && p_73864_1_ >= varinfow && p_73864_1_ <= varinfow + 117 && p_73864_2_ >= this.rectDownload_u && p_73864_2_ <= this.rectDownload_w)
            {
            	if(!Menu.instance.changeLog.equals(null))
            	{
                	GuiChangeLog guichangelog = new GuiChangeLog(this, Menu.instance.changeLog);
                    this.mc.displayGuiScreen(guichangelog);
            	}

            }
        }
    }
    
    public NewServerPinger getOldServerPinger()
    {
        return this.oldServerPinger;
    }
    public static boolean getServerStatus()
    {
    	if(!Menu.instance.jsonString.equals(null))
    	{
			JSONObject jsonobj = new JSONObject(Menu.instance.jsonString);
			boolean status = jsonobj.getBoolean("OK");
			if(!status) return false;
			return true;
    	}
    	return true;
    }   
    public static boolean isOutdate() {
		if (!Menu.instance.jsonString.equals(null)) {
			JSONObject jsonobj = new JSONObject(Menu.instance.jsonString);
			String newversion = jsonobj.getString("Version");
			String downloadurl = jsonobj.getString("DownloadURL");
			if(ConfigVar.debug) newversion = "DEBUG";
			if (!newversion.equalsIgnoreCase(ConfigVar.version)) {
				downloadUrl = downloadurl;
				newVersion = newversion;
				return true;
			} else {
				return false;
			}

		} else {
			return false;
		}
	}
    public static String getAnnouncement() {
    	if (!Menu.instance.jsonString.equals(null)){
			JSONObject jsonobj1 = new JSONObject(Menu.instance.jsonString);
			if(!getServerStatus()) return "§c" + jsonobj1.getString("StatusText");
			return jsonobj1.getString("Announcement");
		} else {
			if(ConfigVar.onlinecheck) return "§c网络出错";
			return ConfigVar.announcement;
		}
    }
}
