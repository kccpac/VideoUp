/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.core.ApplicationPart;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Streaming;
import retrofit.mime.*;


@Controller
public class videoController implements VideoSvcApi {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */
	/*
	videoController()
	{
		return;
	}
*/
	Collection<Video> vv = new ArrayList<Video>();
	
	@RequestMapping(value =VIDEO_SVC_PATH,  method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		// TODO Auto-generated method stub		
		
		return vv;
	}

	@RequestMapping(value = VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(
			@RequestBody Video v) {
		// TODO Auto-generated method stub
		long id = vv.size()+1;
		v.setId(id);
		v.setDataUrl(getDataUrl(id));
		vv.add(v);
		return v;
	}
	
    private String getDataUrl(long videoId){
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }

 	private String getUrlBaseForLocalServer() {
	   HttpServletRequest request = 
	       ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	   String base = 
	      "http://"+request.getServerName() 
	      + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
	   return base;
	}

	@RequestMapping(value = VIDEO_DATA_PATH, method = RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData (
			@PathVariable(value=ID_PARAMETER) long id,
			@RequestParam(value=DATA_PARAMETER) MultipartFile  videoData,
			HttpServletResponse response
			) {
		// TODO Auto-generated method stub
		VideoFileManager manager = null;

		int ret = HttpServletResponse.SC_NOT_FOUND;
		try 
		{
			
			Object v[] = vv.toArray();
			Video video = null;
			int i=0;
			for (i=0; i<v.length; i++)
			{
				if (((Video)v[i]).getId() == id)
				{
					video = (Video) v[i]; 
				 	break;
				} 
			}
			
			if (video != null)
			{
				manager = VideoFileManager.get();
				manager.saveVideoData(video, videoData.getInputStream());
	
				ret = HttpServletResponse.SC_OK;
			}

		}
		catch(Exception e)
		{
		}


		VideoStatus status = new VideoStatus(VideoState.READY);
		response.setStatus(ret);
		return status ;
	}
	


	@Streaming
	@RequestMapping(value = VIDEO_DATA_PATH, method = RequestMethod.GET)
	public @ResponseBody Response getData(
			@PathVariable (value=ID_PARAMETER) long id,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		List <Header>  hlist = new ArrayList<Header>();

		TypedFile tFile = null;
		File file = null;
		int ret = HttpServletResponse.SC_NOT_FOUND;
		try 
		{
			VideoFileManager manager = VideoFileManager.get();

			Object v[] = vv.toArray();
			Video video = null;
			for (int i=0; i<v.length; i++)
			{
				if (((Video) v[i]).getId() == id)
				{
					video = (Video) v[i];
					break;
				}
			 	i++;
			} 
			
			
			if (video != null)
			{
				
				String ctype = video.getContentType();
				String strUrl=video.getDataUrl();
				file = (File) new File(strUrl);
				tFile = new TypedFile(ctype, file);

				manager.copyVideoData(video, (OutputStream) response.getOutputStream());
				ret = HttpServletResponse.SC_OK;
			}
	
		}
		catch(Exception e)
		{
		}

		Response resp = new Response(new String(), ret, new String(), hlist, tFile);
		response.setStatus(ret);
	

		return resp;
	}

	@Override
	public VideoStatus setVideoData(long id, TypedFile videoData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getData(long id) {
		// TODO Auto-generated method stub
		return null;
	}
	


	
}
