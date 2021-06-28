package com.infy.infyinterns.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.infy.infyinterns.dto.MentorDTO;
import com.infy.infyinterns.dto.ProjectDTO;
import com.infy.infyinterns.entity.Mentor;
import com.infy.infyinterns.entity.Project;
import com.infy.infyinterns.exception.InfyInternException;
import com.infy.infyinterns.repository.MentorRepository;
import com.infy.infyinterns.repository.ProjectRepository;

@Service("projectdService")
@Transactional
public class ProjectAllocationServiceImpl implements ProjectAllocationService {
	
	@Autowired
	private ProjectRepository projectRepository;
	
	@Autowired
	private MentorRepository mentorRepository;

	@Override
	public Integer allocateProject(ProjectDTO project) throws InfyInternException {
		Mentor mentor = mentorRepository.findById(project.getMentorDTO().getMentorId())
				.orElseThrow(()->new InfyInternException("Service.MENTOR_NOT_FOUND"));
		
		if(mentor.getNumberOfProjectsMentored()>=3) {
			throw new InfyInternException("Service.CANNOT_ALLOCATE_PROJECT");
		}
		
		Project projectEntity = new Project();
		projectEntity.setIdeaOwner(project.getIdeaOwner());
		projectEntity.setMentor(mentor);
		projectEntity.setProjectName(project.getProjectName());
		projectEntity.setReleaseDate(project.getReleaseDate());
		mentor.setNumberOfProjectsMentored(mentor.getNumberOfProjectsMentored()+1);

		return projectRepository.save(projectEntity).getProjectId();
	}

	
	@Override
	public List<MentorDTO> getMentors(Integer numberOfProjectsMentored) throws InfyInternException {
		List<Mentor> mentors = mentorRepository.findByNumberOfProjectsMentored(numberOfProjectsMentored);
		if(mentors.isEmpty()) {
			throw new InfyInternException("Service.MENTOR_NOT_FOUND");
		}
		return mentors.stream().map(mentor->new MentorDTO(mentor.getMentorId(), mentor.getMentorName(), mentor.getNumberOfProjectsMentored()))
				.collect(Collectors.toList());
		
	}


	@Override
	public void updateProjectMentor(Integer projectId, Integer mentorId) throws InfyInternException {
		Mentor mentor = mentorRepository.findById(mentorId)
				.orElseThrow(()->new InfyInternException("Service.MENTOR_NOT_FOUND"));
		
		if(mentor.getNumberOfProjectsMentored()>=3) {
			throw new InfyInternException("Service.CANNOT_ALLOCATE_PROJECT");
		}
		
		Project project = projectRepository.findById(projectId)
				.orElseThrow(()->new InfyInternException("Service.PROJECT_NOT_FOUND"));
		
		project.setMentor(mentor);
		mentor.setNumberOfProjectsMentored(mentor.getNumberOfProjectsMentored()+1);
		
	}

	@Override
	public void deleteProject(Integer projectId) throws InfyInternException {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(()->new InfyInternException("Service.PROJECT_NOT_FOUND"));
		if(project.getMentor()!=null) {
			Mentor mentor = project.getMentor();
			mentor.setNumberOfProjectsMentored(mentor.getNumberOfProjectsMentored()-1);
			
		}
		projectRepository.delete(project);
		
	}
}