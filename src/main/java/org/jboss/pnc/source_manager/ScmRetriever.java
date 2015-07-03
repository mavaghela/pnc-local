package org.jboss.pnc.source_manager;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.manager.BasicScmManager;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.git.jgit.JGitScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.io.File;


public class ScmRetriever {

    private ScmManager scmManager;
    private SCMRepositoryType repositoryType;

    public ScmRetriever(SCMRepositoryType repoType){
        this.scmManager = new BasicScmManager();
        this.repositoryType = repoType;
        setProvider(repositoryType);
    }

    public void cloneRepository(String scmUrl, String revision, String cloneTo) throws Exception {

        File buildDir = new File(cloneTo);
        if (!buildDir.exists())
            buildDir.mkdir();

        ScmRepository repo = getScmRepository(String.format("scm:%s:%s", repositoryType.name(), scmUrl), scmManager);
        scmManager.checkOut(repo, new ScmFileSet(buildDir), new ScmTag(revision));


    }

    private void setProvider(SCMRepositoryType type){
        switch (type) {
            case GIT:
                scmManager.setScmProvider(type.name(), new JGitScmProvider());
        }
    }

    private ScmRepository getScmRepository( String scmUrl, ScmManager scmManager ) throws Exception {
        ScmRepository repository;
        try
        {
            return scmManager.makeScmRepository( scmUrl );
        }
        catch ( NoSuchScmProviderException ex )
        {
            throw new Exception( "Could not find a provider.", ex );
        }
        catch ( ScmRepositoryException ex )
        {
            throw new Exception( "Error while connecting to the repository", ex );
        }
    }
}