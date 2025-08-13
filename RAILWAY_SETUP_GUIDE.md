# Railway Deployment Setup Guide - Chimera MVP

## Step-by-Step Railway Setup

### Step 1: Create Railway Account (2 minutes)
1. Go to **https://railway.app**
2. Click **"Login"** (top right)
3. Choose **"GitHub"** login (recommended for auto-deploy)
4. Authorize Railway to access your GitHub account
5. You'll get **500 hours/month free** execution time

### Step 2: Prepare Your GitHub Repository (5 minutes)
**IMPORTANT: Make sure your code is on GitHub first!**

```bash
# If you haven't pushed to GitHub yet:
cd "D:\Chimera MVP"
git init
git add .
git commit -m "Initial commit - Flutter/Gradle migration complete"

# Create GitHub repository and push
# (Use GitHub Desktop or command line)
git remote add origin https://github.com/YOUR_USERNAME/chimera-mvp.git
git push -u origin main
```

### Step 3: Deploy Backend on Railway (10 minutes)

#### 3a. Create New Project
1. In Railway dashboard, click **"New Project"**
2. Choose **"Deploy from GitHub repo"**
3. Select your **chimera-mvp** repository
4. Railway will automatically detect the Dockerfile in `backend/`

#### 3b. Configure Build Settings
1. In Railway project settings:
   - **Root Directory**: Leave empty (Railway will find backend/Dockerfile)
   - **Build Command**: Automatic (Docker build)
   - **Start Command**: Automatic (from Dockerfile)

#### 3c. Add Database
1. In your Railway project, click **"New Service"**
2. Choose **"Database"** → **"PostgreSQL"**
3. Railway will automatically:
   - Create PostgreSQL database
   - Generate `DATABASE_URL` environment variable
   - Connect it to your backend service

### Step 4: Set Environment Variables (5 minutes)
In Railway project → **Variables** tab, add:

```bash
# API Keys (your provided keys)
OPENAI_API_KEY=[Your OpenAI API key]
NEWS_API_KEY=[Your News API key]

# Spring Profile
SPRING_PROFILES_ACTIVE=railway

# Optional - for better logging
LOGGING_LEVEL_COM_CHIMERA=INFO
```

### Step 5: Deploy and Test (10 minutes)

#### 5a. Trigger Deployment
1. Railway should automatically start building after you set environment variables
2. Watch the **"Deployments"** tab for build progress
3. Build takes ~3-5 minutes (downloading dependencies, building Docker image)

#### 5b. Get Your App URL
1. Go to **"Settings"** → **"Environment"**
2. Copy the **"Public URL"** (looks like `https://chimera-backend-production-xxxx.up.railway.app`)
3. **Save this URL** - you'll need it for Flutter app configuration

#### 5c. Test Your Backend
Open these URLs in browser:
```
https://your-app.railway.app/actuator/health
https://your-app.railway.app/actuator/info
https://your-app.railway.app/health/ready
```

**Expected Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}
```

## Step 6: Configure Flutter App (15 minutes)

### 6a. Update Flutter API Endpoints
Edit `chimera_flutter/lib/data/services/api_service.dart`:

```dart
class ApiService {
  // Replace with your Railway URL
  static const String baseUrl = 'https://your-app.railway.app';
  
  static const String rankEndpoint = '$baseUrl/api/rank';
  static const String chatEndpoint = '$baseUrl/api/chat';
  static const String healthEndpoint = '$baseUrl/actuator/health';
}
```

### 6b. Test Flutter App Locally
```bash
cd chimera_flutter
flutter pub get
flutter run
# Test with your Railway backend
```

## Step 7: Monitor Your Deployment

### Railway Dashboard Monitoring
1. **Metrics**: CPU, Memory, Network usage
2. **Logs**: Real-time application logs
3. **Deployments**: Build history and status
4. **Usage**: Track your 500 hours/month limit

### Key URLs to Bookmark:
- **Main App**: `https://your-app.railway.app`
- **Health Check**: `https://your-app.railway.app/actuator/health`
- **Railway Dashboard**: `https://railway.app/dashboard`

## Troubleshooting Common Issues

### ❌ Build Failed
**Problem**: Docker build fails
**Solution**: 
- Check **Deployments** tab for error logs
- Ensure `backend/gradlew` has executable permissions
- Verify `backend/Dockerfile` exists

### ❌ Database Connection Error
**Problem**: "Connection refused" in logs
**Solution**:
- Ensure PostgreSQL service is running
- Check `DATABASE_URL` environment variable is set
- Wait 2-3 minutes after database creation

### ❌ API Keys Not Working
**Problem**: OpenAI API errors
**Solution**:
- Verify `OPENAI_API_KEY` in Railway Variables
- Check for trailing spaces in API key
- Restart deployment after adding variables

### ❌ CORS Errors from Flutter
**Problem**: Flutter can't connect to Railway backend
**Solution**:
- Add your Railway URL to CORS configuration
- Check Flutter app is using HTTPS Railway URL

## Cost Management

### Free Tier Limits
- **500 hours/month** execution time
- **100GB/month** bandwidth
- **1GB** PostgreSQL storage
- **Unlimited** deployments

### Monitor Usage
- Check **Usage** tab in Railway dashboard
- Set up alerts when approaching limits
- Optimize API calls to reduce execution time

## Next Steps After Deployment

### ✅ Immediate Tasks:
1. **Test all endpoints** with Postman/curl
2. **Verify Flutter app** connects successfully
3. **Monitor logs** for any errors
4. **Document your Railway URL** for team use

### ✅ Phase M8 Preparation:
1. **Performance baseline**: Record response times
2. **Load testing**: Test with multiple concurrent requests
3. **Data validation**: Ensure ranking and chat APIs work
4. **Error handling**: Test failure scenarios

## Support & Resources

- **Railway Docs**: https://docs.railway.app
- **Discord Support**: https://railway.app/discord
- **Status Page**: https://status.railway.app

---

## Quick Summary Checklist

- [ ] ✅ Railway account created
- [ ] ✅ GitHub repository connected
- [ ] ✅ PostgreSQL database added
- [ ] ✅ Environment variables set
- [ ] ✅ Backend deployment successful
- [ ] ✅ Health endpoints working
- [ ] ✅ Flutter app updated with Railway URL
- [ ] ✅ End-to-end testing complete

**Your Railway URL**: `___________________________`

**Next Phase**: M8 - Validation & Calibration