package br.com.httpdroid.interfaces;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Criado por Felipe Samuel em 07/01/2019.
 */
public interface IMethods<Object> {

    Object get(int id) throws IOException;
    List<Object> get() throws IOException;

    Object post(Object object) throws IOException;
    Object put(Object object) throws IOException, NoSuchFieldException, IllegalAccessException;

    boolean delete(int id) throws IOException;

    Object upload(File file) throws IOException;

}
