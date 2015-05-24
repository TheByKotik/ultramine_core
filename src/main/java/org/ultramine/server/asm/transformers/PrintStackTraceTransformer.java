package org.ultramine.server.asm.transformers;

import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class PrintStackTraceTransformer implements IClassTransformer
{
	private static final Logger log = LogManager.getLogger();
	
	private static final String UMHOOKS_TYPE = "org/ultramine/server/UMHooks";
	private static final Set<String> THROWABLE_TYPES = new HashSet<String>();
	private static final String PST_NAME = "printStackTrace";
	private static final String PST_DESC = "()V";
	private static final String UM_PST_DESC = "(Ljava/lang/Throwable;)V";
	
	static
	{
		THROWABLE_TYPES.add("java/lang/Throwable");
		THROWABLE_TYPES.add("java/lang/Exception");
		THROWABLE_TYPES.add("java/lang/RuntimeException");
		THROWABLE_TYPES.add("java/io/IOException");
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if(basicClass == null)
			return null;
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);

		for(MethodNode m: classNode.methods)
		{
			for(ListIterator<AbstractInsnNode> it = m.instructions.iterator(); it.hasNext(); )
			{
				AbstractInsnNode insnNode = it.next();
				if(insnNode.getType() == AbstractInsnNode.METHOD_INSN)
				{
					MethodInsnNode fi = (MethodInsnNode)insnNode;
					if(THROWABLE_TYPES.contains(fi.owner) && PST_NAME.equals(fi.name) && PST_DESC.equals(fi.desc) && fi.getOpcode() == Opcodes.INVOKEVIRTUAL)
					{
						log.trace("Method {}.{}{}: Replacing INVOKEVIRTUAL {}.printStackTrace with INVOKESTATIC UMHooks.printStackTrace", name, m.name, m.desc, fi.owner);
						it.remove();
						MethodInsnNode replace = new MethodInsnNode(Opcodes.INVOKESTATIC, UMHOOKS_TYPE, PST_NAME, UM_PST_DESC, false);
						it.add(replace);
					}
				}
			}
		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}
}
