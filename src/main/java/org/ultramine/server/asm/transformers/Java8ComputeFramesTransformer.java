package org.ultramine.server.asm.transformers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.ultramine.server.asm.ComputeFramesClassWriter;

import net.minecraft.launchwrapper.IClassTransformer;

public class Java8ComputeFramesTransformer implements IClassTransformer
{
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if(basicClass == null)
			return null;
		ClassReader classReader = new ClassReader(basicClass);
		if(classReader.readInt(classReader.getItem(1) - 7) <= Opcodes.V1_6)
			return basicClass;

		ClassWriter writer = new ComputeFramesClassWriter();
		classReader.accept(writer, 0);
		return writer.toByteArray();
	}
}
